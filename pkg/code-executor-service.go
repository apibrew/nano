package nano

import (
	"context"
	"encoding/base64"
	"errors"
	"fmt"
	"github.com/apibrew/apibrew/pkg/service"
	backend_event_handler "github.com/apibrew/apibrew/pkg/service/backend-event-handler"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/apibrew/nano/pkg/abs"
	"github.com/apibrew/nano/pkg/addons"
	"github.com/apibrew/nano/pkg/model"
	util2 "github.com/apibrew/nano/pkg/util"
	"github.com/clarkmcc/go-typescript"
	"github.com/dop251/goja"
	"github.com/dop251/goja_nodejs/require"
	log "github.com/sirupsen/logrus"
	"strconv"
	"strings"
	"time"
)

type codeExecutorService struct {
	container           service.Container
	backendEventHandler backend_event_handler.BackendEventHandler
	codeContext         util2.Map[string, *codeExecutionContext]
	globalObject        *globalObject
	functions           util2.Map[string, string]
	modules             util2.Map[string, string]
	systemModules       util2.Map[string, string]
	registry            *require.Registry
}

func (s *codeExecutorService) GetContainer() service.Container {
	return s.container
}

func (s *codeExecutorService) GetBackendEventHandler() backend_event_handler.BackendEventHandler {
	return s.backendEventHandler
}

func (s *codeExecutorService) GetGlobalObject() abs.GlobalObject {
	return s.globalObject
}

func (s *codeExecutorService) RunScript(ctx context.Context, script *model.Script) (output interface{}, err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("panic: %v", r)
		}
	}()

	var source = script.Source

	decodedBytes, err := base64.StdEncoding.DecodeString(script.Source)

	if err == nil {
		source = string(decodedBytes)
	}

	if script.Language == model.ScriptLanguage_TYPESCRIPT {
		transpiled, err := typescript.TranspileCtx(ctx, strings.NewReader(source), s.typescriptOptions)

		if err != nil {
			return nil, err
		}

		source = transpiled

		source = "exports = {};" + source
	}

	log.Debug("Registering script: " + script.Id.String())

	log.Println(source)

	vm := goja.New()
	vm.SetFieldNameMapper(goja.UncapFieldNameMapper())

	s.registry.Enable(vm)

	cec := &codeExecutionContext{}
	ctx, cancel := context.WithCancel(util.WithSystemContext(ctx))
	cec.ctx = ctx
	cec.cancel = cancel
	cec.identifier = script.Id.String() + "-" + strconv.Itoa(int(script.Version))
	cec.scriptMode = true
	err = s.registerBuiltIns("["+cec.identifier+"]", vm, cec)

	if err != nil {
		return nil, err
	}

	result, err := vm.RunString(source)

	if err != nil {
		return nil, err
	}

	return result.Export(), nil
}

func (s *codeExecutorService) RunInlineScript(ctx context.Context, identifier string, source string) (result any, err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("panic: %v", r)
		}
	}()

	vm := goja.New()
	vm.SetFieldNameMapper(goja.UncapFieldNameMapper())

	s.registry.Enable(vm)

	cec := &codeExecutionContext{}
	ctx, cancel := context.WithCancel(util.WithSystemContext(ctx))
	cec.ctx = ctx
	cec.cancel = cancel
	cec.identifier = identifier
	cec.scriptMode = true
	err = s.registerBuiltIns("["+cec.identifier+"]", vm, cec)

	if err != nil {
		return nil, err
	}

	var res goja.Value
	res, err = vm.RunString(source)

	if err != nil {
		return nil, err
	}

	return res.Export(), nil
}

func (s *codeExecutorService) registerCode(code *model.Code) (err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("panic: %v", r)
		}
	}()
	decodedBytes, err := base64.StdEncoding.DecodeString(code.Content)

	var source = code.Content

	if err == nil {
		source = string(decodedBytes)
	}

	if code.Language == model.CodeLanguage_TYPESCRIPT {
		transpiled, err := typescript.TranspileCtx(context.TODO(), strings.NewReader(source), s.typescriptOptions)

		if err != nil {
			return err
		}

		source = "exports = {};" + transpiled
	}

	log.Debug("Registering code: " + code.Name)

	program, err := goja.Compile(code.Name, source, false)

	if err != nil {
		return err
	}

	cec := &codeExecutionContext{}
	ctx, cancel := context.WithCancel(util.WithSystemContext(context.Background()))
	cec.ctx = ctx
	cec.cancel = cancel

	cec.handlerMap = util2.NewConcurrentSyncMap[string, *abs.HandlerData]()
	cec.identifier = code.Id.String() + "-" + strconv.Itoa(int(code.Version))
	s.codeContext.Set(code.Name, cec)

	var concurrencyLevel = 8
	if code.ConcurrencyLevel != nil {
		concurrencyLevel = int(*code.ConcurrencyLevel)
	}

	for i := 0; i < concurrencyLevel; i++ {
		vm := goja.New()
		s.registry.Enable(vm)
		vm.SetFieldNameMapper(goja.UncapFieldNameMapper())
		err = s.registerBuiltIns(code.Name, vm, cec)

		if err != nil {
			return err
		}

		_, err = vm.RunProgram(program)
		if err != nil {
			return err
		}

		cec.vms = append(cec.vms, vm)
	}

	return nil
}

func (s *codeExecutorService) updateCode(code *model.Code) error {
	if err := s.unRegisterCode(code); err != nil {
		return err
	}

	if err := s.registerCode(code); err != nil {
		return err
	}

	return nil
}

func (s *codeExecutorService) unRegisterCode(code *model.Code) error {
	cec, ok := s.codeContext.Find(code.Name)

	if !ok {
		return errors.New("code context not found")
	}

	cec.cancel()

	return nil
}

func (s *codeExecutorService) registerBuiltIns(codeName string, vm *goja.Runtime, cec *codeExecutionContext) error {
	if err := addons.Register(vm, cec, s, codeName, s.container); err != nil {
		return err
	}

	if err := vm.Set("global", s.globalObject); err != nil {
		return err
	}

	if err := s.registerTimeoutFunctions(vm, cec); err != nil {
		return err
	}

	if _, err := vm.RunScript("resource.js", GetBuiltinJs("resource.js")); err != nil {
		return err
	}

	if _, err := vm.RunScript("targetResource.js", GetBuiltinJs("targetResource.js")); err != nil {
		return err
	}

	if _, err := vm.RunScript("transactional.js", GetBuiltinJs("transactional.js")); err != nil {
		return err
	}

	if !cec.scriptMode {
		if _, err := vm.RunScript("handle.js", GetBuiltinJs("handle.js")); err != nil {
			return err
		}
	}

	// register functions

	for _, name := range s.functions.Keys() {
		handler := s.functions.Get(name)
		var fn, err = vm.RunString(handler)

		if err != nil {
			return err
		}

		if err = vm.Set(name, fn); err != nil {
			return err
		}
	}

	return nil
}

func (s *codeExecutorService) registerTimeoutFunctions(vm *goja.Runtime, cec *codeExecutionContext) error {
	if err := vm.Set("setTimeout", s.setTimeoutFn(cec)); err != nil {
		return err
	}

	if err := vm.Set("clearTimeout", s.clearTimeoutFn(cec)); err != nil {
		return err
	}

	if err := vm.Set("setInterval", s.setIntervalFn(cec)); err != nil {
		return err
	}

	if err := vm.Set("clearInterval", s.clearIntervalFn(cec)); err != nil {
		return err
	}

	if err := vm.Set("sleep", s.sleepFn(cec)); err != nil {
		return err
	}

	return nil
}

func (s *codeExecutorService) setTimeoutFn(cec *codeExecutionContext) func(fn func(), duration int64) func() {
	return func(fn func(), duration int64) func() {
		cancel := make(chan struct{})
		cancelFn := func() {
			close(cancel)
		}

		go func() {
			defer func() {
				if r := recover(); r != nil {
					log.Warn(r)
				}
			}()
			select {
			case <-time.After(time.Duration(duration) * time.Millisecond):
				fn()
			case <-cancel:
			case <-cec.ctx.Done():
				// Cancel the timeout
			}
		}()
		return cancelFn
	}
}

func (s *codeExecutorService) clearTimeoutFn(cec *codeExecutionContext) func(clearFn func()) {
	return func(clearFn func()) {
		defer func() {
			if r := recover(); r != nil {
				log.Warn(r)
			}
		}()
		clearFn()
	}
}

func (s *codeExecutorService) setIntervalFn(cec *codeExecutionContext) func(fn func(), duration int64) func() {
	return func(fn func(), duration int64) func() {
		cancel := make(chan struct{})
		cancelFn := func() {
			close(cancel)
		}

		go func() {
		Loop:
			for {
				defer func() {
					if r := recover(); r != nil {
						log.Warn(r)
					}
				}()
				select {
				case <-time.After(time.Duration(duration) * time.Millisecond):
					fn()
				case <-cec.ctx.Done():
					break Loop
				case <-cancel:
					break Loop
				}
			}
		}()
		return cancelFn
	}
}

func (s *codeExecutorService) clearIntervalFn(cec *codeExecutionContext) func(clearFn func()) {
	return func(clearFn func()) {
		defer func() {
			if r := recover(); r != nil {
				log.Warn(r)
			}
		}()
		clearFn()
	}
}

func (s *codeExecutorService) sleepFn(cec *codeExecutionContext) func(duration int32) {
	return func(duration int32) {
		select {
		case <-time.After(time.Duration(duration) * time.Millisecond):
		case <-cec.ctx.Done():
		}
	}
}

func (s *codeExecutorService) registerFunction(function *model.Function) error {
	var handler = fmt.Sprintf(`(function(a, b, c, d, e, f, g, h, i, j, k, l, m, n) {%s})`, function.Source)

	s.functions.Set(function.Name, handler)

	for _, cctx := range s.codeContext.Values() {
		for _, vm := range cctx.vms {
			err := vm.Try(func() {
				var fn, err = vm.RunString(handler)

				if err != nil {
					log.Error(err)
				}

				err = vm.Set(function.Name, fn)

				if err != nil {
					log.Error(err)
				}
			})

			if err != nil {
				log.Error(err)
			}
		}

	}

	return nil
}

func (s *codeExecutorService) updateFunction(function *model.Function) error {
	return s.registerFunction(function)
}

func (s *codeExecutorService) unRegisterFunction(function *model.Function) error {
	s.functions.Delete(function.Name)

	for _, cctx := range s.codeContext.Values() {
		for _, vm := range cctx.vms {
			err := vm.Try(func() {
				err := vm.Set(function.Name, nil)

				if err != nil {
					log.Error(err)
				}
			})

			if err != nil {
				log.Error(err)
			}
		}
	}

	return nil
}

func (s *codeExecutorService) typescriptOptions(config *typescript.Config) {
	config.Verbose = true
	config.CompileOptions = map[string]interface{}{
		"module": "commonJS",
		"target": "es5",
	}
}

func (s *codeExecutorService) registerModule(module *model.Module) error {
	var source = module.Source

	decodedBytes, err := base64.StdEncoding.DecodeString(module.Source)

	if err == nil {
		source = string(decodedBytes)
	}

	if module.Language == model.ModuleLanguage_TYPESCRIPT {
		transpiled, err := typescript.TranspileCtx(context.TODO(), strings.NewReader(source), s.typescriptOptions)

		if err != nil {
			return err
		}

		source = transpiled
	}

	s.modules.Set(module.Name, source)

	log.Println("Registering module: " + module.Name)

	return nil
}

func (s *codeExecutorService) updateModule(module *model.Module) error {
	if err := s.unRegisterModule(module); err != nil {
		return err
	}

	return s.registerModule(module)
}

func (s *codeExecutorService) unRegisterModule(module *model.Module) error {
	s.modules.Delete(module.Name)

	return nil
}

func (s *codeExecutorService) srcLoader(path string) ([]byte, error) {
	if strings.HasPrefix(path, "node_modules/") {
		path = path[12:]
	}

	if strings.HasPrefix(path, "./") {
		path = path[2:]
	}

	if strings.HasPrefix(path, "/") {
		path = path[1:]
	}

	if source, ok := s.systemModules.Find(path); ok {
		return []byte(source), nil
	}

	if source, ok := s.modules.Find(path); ok {
		return []byte(source), nil
	}

	return nil, errors.New("module not found with name: " + path)
}

func (s *codeExecutorService) init() {
	s.systemModules.Set("@apibrew/nano", GetBuiltinJs("nano.js"))
}

func newCodeExecutorService(container service.Container, backendEventHandler backend_event_handler.BackendEventHandler) *codeExecutorService {
	ces := &codeExecutorService{
		container:           container,
		backendEventHandler: backendEventHandler,
		codeContext:         util2.NewConcurrentSyncMap[string, *codeExecutionContext](),
		systemModules:       util2.NewConcurrentSyncMap[string, string](),
		modules:             util2.NewConcurrentSyncMap[string, string](),
		globalObject:        newGlobalObject(),
		functions:           util2.NewConcurrentSyncMap[string, string](),
	}

	ces.init()

	registry := require.NewRegistryWithLoader(ces.srcLoader)

	ces.registry = registry

	return ces
}
