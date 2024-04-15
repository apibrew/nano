package nano

import (
	"context"
	"encoding/base64"
	"fmt"
	"github.com/apibrew/apibrew/pkg/service"
	backend_event_handler "github.com/apibrew/apibrew/pkg/service/backend-event-handler"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/apibrew/nano/pkg/abs"
	"github.com/apibrew/nano/pkg/addons"
	"github.com/apibrew/nano/pkg/model"
	"github.com/dop251/goja"
	"github.com/dop251/goja_nodejs/require"
	log "github.com/sirupsen/logrus"
	"strconv"
	"time"
)

type codeExecutorService struct {
	container           service.Container
	backendEventHandler backend_event_handler.BackendEventHandler
	codeContext         map[string]*codeExecutionContext
	globalObject        *globalObject
	functions           map[string]string
}

func (s codeExecutorService) NewVm(options abs.VmOptions) (*goja.Runtime, error) {
	vm := goja.New()
	vm.SetFieldNameMapper(goja.UncapFieldNameMapper())

	registry := new(require.Registry) // this can be shared by multiple runtimes

	runtime := goja.New()
	registry.Enable(runtime)

	cec := &codeExecutionContext{}
	cec.ctx = util.WithSystemContext(context.Background())
	err := s.registerBuiltIns("", vm, cec)

	if err != nil {
		return nil, err
	}

	return vm, nil
}

func (s codeExecutorService) GetContainer() service.Container {
	return s.container
}

func (s codeExecutorService) GetBackendEventHandler() backend_event_handler.BackendEventHandler {
	return s.backendEventHandler
}

func (s codeExecutorService) GetGlobalObject() abs.GlobalObject {
	return s.globalObject
}

func (s codeExecutorService) runScript(ctx context.Context, script *model.Script) (output interface{}, err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("%v", r)
		}
	}()

	decodedBytes, err := base64.StdEncoding.DecodeString(script.Source)

	if err == nil {
		script.Source = string(decodedBytes)
	}

	log.Debug("Registering script: " + script.Id.String())

	vm := goja.New()
	vm.SetFieldNameMapper(goja.UncapFieldNameMapper())

	registry := new(require.Registry) // this can be shared by multiple runtimes

	runtime := goja.New()
	registry.Enable(runtime)

	cec := &codeExecutionContext{}
	cec.ctx = util.WithSystemContext(context.Background())
	cec.vm = vm
	cec.identifier = script.Id.String() + "-" + strconv.Itoa(int(script.Version))
	cec.scriptMode = true
	err = s.registerBuiltIns("["+cec.identifier+"]", vm, cec)

	s.codeContext[cec.identifier] = cec

	if err != nil {
		return nil, err
	}

	result, err := vm.RunString(script.Source)

	if err != nil {
		return nil, err
	}

	return result.Export(), nil
}

func (s codeExecutorService) runInlineScript(ctx context.Context, identifier string, source string) (err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("%v", r)
		}
	}()
	vm := goja.New()
	vm.SetFieldNameMapper(goja.UncapFieldNameMapper())

	registry := new(require.Registry) // this can be shared by multiple runtimes

	runtime := goja.New()
	registry.Enable(runtime)

	cec := &codeExecutionContext{}
	cec.ctx = util.WithSystemContext(context.Background())
	cec.vm = vm
	cec.identifier = identifier
	cec.scriptMode = true
	err = s.registerBuiltIns("["+cec.identifier+"]", vm, cec)

	s.codeContext[cec.identifier] = cec

	if err != nil {
		return err
	}

	_, err = vm.RunString(source)

	if err != nil {
		return err
	}

	return nil
}

func (s codeExecutorService) registerCode(code *model.Code) (err error) {
	defer func() {
		if r := recover(); r != nil {
			err = fmt.Errorf("%v", r)
		}
	}()
	decodedBytes, err := base64.StdEncoding.DecodeString(code.Content)

	if err == nil {
		code.Content = string(decodedBytes)
	}

	log.Debug("Registering code: " + code.Name)

	vm := goja.New()
	vm.SetFieldNameMapper(goja.UncapFieldNameMapper())

	registry := new(require.Registry) // this can be shared by multiple runtimes

	runtime := goja.New()
	registry.Enable(runtime)

	cec := &codeExecutionContext{}
	cec.ctx = util.WithSystemContext(context.Background())
	cec.vm = vm
	cec.identifier = code.Id.String() + "-" + strconv.Itoa(int(code.Version))
	err = s.registerBuiltIns(code.Name, vm, cec)

	s.codeContext[code.Name] = cec

	if err != nil {
		return err
	}

	_, err = vm.RunString(code.Content)
	if err != nil {
		return err
	}

	return nil
}

func (s codeExecutorService) updateCode(code *model.Code) error {
	if err := s.unRegisterCode(code); err != nil {
		return err
	}

	if err := s.registerCode(code); err != nil {
		return err
	}

	return nil
}

func (s codeExecutorService) unRegisterCode(code *model.Code) error {
	cec := s.codeContext[code.Name]
	if len(cec.handlerIds) > 0 {
		for _, handlerId := range s.codeContext[code.Name].handlerIds {
			s.backendEventHandler.UnRegisterHandler(backend_event_handler.Handler{
				Id: handlerId,
			})
		}
	}

	for _, cancelFn := range cec.closeHandlers {
		cancelFn()
	}

	return nil
}

func (s codeExecutorService) registerBuiltIns(codeName string, vm *goja.Runtime, cec *codeExecutionContext) error {
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

	if !cec.scriptMode {
		if _, err := vm.RunScript("handle.js", GetBuiltinJs("handle.js")); err != nil {
			return err
		}
	}

	// register functions

	for name, handler := range s.functions {
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

func (s codeExecutorService) registerTimeoutFunctions(vm *goja.Runtime, cec *codeExecutionContext) error {
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

func (s codeExecutorService) setTimeoutFn(cec *codeExecutionContext) func(fn func(), duration int64) func() {
	return func(fn func(), duration int64) func() {
		cancel := make(chan struct{})
		cancelFn := func() {
			close(cancel)
		}
		cec.closeHandlers = append(cec.closeHandlers, cancelFn) // fixme (potential memory leak)

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
				// Cancel the timeout
			}
		}()
		return cancelFn
	}
}

func (s codeExecutorService) clearTimeoutFn(cec *codeExecutionContext) func(clearFn func()) {
	return func(clearFn func()) {
		defer func() {
			if r := recover(); r != nil {
				log.Warn(r)
			}
		}()
		clearFn()
	}
}

func (s codeExecutorService) setIntervalFn(cec *codeExecutionContext) func(fn func(), duration int64) func() {
	return func(fn func(), duration int64) func() {
		cancel := make(chan struct{})
		cancelFn := func() {
			close(cancel)
		}
		cec.closeHandlers = append(cec.closeHandlers, cancelFn) // fixme (potential memory leak)

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
				case <-cancel:
					break Loop
				}
			}
		}()
		return cancelFn
	}
}

func (s codeExecutorService) clearIntervalFn(cec *codeExecutionContext) func(clearFn func()) {
	return func(clearFn func()) {
		defer func() {
			if r := recover(); r != nil {
				log.Warn(r)
			}
		}()
		clearFn()
	}
}

func (s codeExecutorService) sleepFn(cec *codeExecutionContext) func(duration int32) {
	return func(duration int32) {
		time.Sleep(time.Duration(duration) * time.Millisecond)
	}
}

func (s *codeExecutorService) registerFunction(function *model.Function) error {
	var handler = fmt.Sprintf(`(function(a, b, c, d, e, f, g, h, i, j, k, l, m, n) {%s})`, function.Source)

	s.functions[function.Name] = handler

	for _, cctx := range s.codeContext {
		err := cctx.vm.Try(func() {
			var fn, err = cctx.vm.RunString(handler)

			if err != nil {
				log.Error(err)
			}

			err = cctx.vm.Set(function.Name, fn)

			if err != nil {
				log.Error(err)
			}
		})

		if err != nil {
			log.Error(err)
		}
	}

	return nil
}

func (s codeExecutorService) updateFunction(function *model.Function) error {
	return s.registerFunction(function)
}

func (s codeExecutorService) unRegisterFunction(function *model.Function) error {
	delete(s.functions, function.Name)

	for _, cctx := range s.codeContext {
		err := cctx.vm.Try(func() {
			err := cctx.vm.Set(function.Name, nil)

			if err != nil {
				log.Error(err)
			}
		})

		if err != nil {
			log.Error(err)
		}
	}

	return nil
}

func newCodeExecutorService(container service.Container, backendEventHandler backend_event_handler.BackendEventHandler) *codeExecutorService {
	return &codeExecutorService{container: container, backendEventHandler: backendEventHandler, codeContext: make(map[string]*codeExecutionContext), globalObject: newGlobalObject(), functions: make(map[string]string)}
}
