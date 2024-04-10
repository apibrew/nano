package nano

import (
	"context"
	"fmt"
	"github.com/apibrew/apibrew/pkg/errors"
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/resources"
	"github.com/apibrew/apibrew/pkg/service"
	backend_event_handler "github.com/apibrew/apibrew/pkg/service/backend-event-handler"
	"github.com/apibrew/apibrew/pkg/util"
	model2 "github.com/apibrew/nano/pkg/model"
	"github.com/sirupsen/logrus"
	"google.golang.org/protobuf/types/known/structpb"
	"log"
)

type module struct {
	container           service.Container
	codeExecutor        *codeExecutorService
	backendEventHandler backend_event_handler.BackendEventHandler
}

func (m module) Init() {
	m.ensureNamespace()
	m.ensureResources()
	m.initCodeListeners()
	m.initFunctionListeners()
	m.initScriptListeners()
	m.initExistingCodes()
	m.initExistingFunctions()
}

func (m module) ensureNamespace() {
	_, err := m.container.GetRecordService().Apply(util.SystemContext, service.RecordUpdateParams{
		Namespace: resources.NamespaceResource.Namespace,
		Resource:  resources.NamespaceResource.Name,
		Records: []*model.Record{
			{
				Properties: map[string]*structpb.Value{
					"name": structpb.NewStringValue("nano"),
				},
			},
		},
	})

	if err != nil {
		log.Panic(err)
	}
}

func (m module) ensureResources() {
	var resources = []*model.Resource{
		model2.CodeResource,
		model2.ScriptResource,
		model2.FunctionResource,
		model2.CronJobResource,
	}

	for _, resource := range resources {
		existingResource, err := m.container.GetResourceService().GetResourceByName(util.SystemContext, resource.Namespace, resource.Name)

		if err == nil {
			resource.Id = existingResource.Id
			err = m.container.GetResourceService().Update(util.SystemContext, resource, true, true)

			if err != nil {
				log.Panic(err)
			}
		} else if err.Is(errors.ResourceNotFoundError) {
			_, err = m.container.GetResourceService().Create(util.SystemContext, resource, true, true)

			if err != nil {
				log.Panic(err)
			}
		} else if err != nil {
			log.Panic(err)
		}
	}
}

func (m module) initCodeListeners() {
	m.backendEventHandler.RegisterHandler(backend_event_handler.Handler{
		Id:   "nano-code-listener",
		Name: "nano-code-listener",
		Fn:   m.codeListenerHandler,
		Selector: &model.EventSelector{
			Actions: []model.Event_Action{
				model.Event_CREATE, model.Event_UPDATE, model.Event_DELETE,
			},
			Namespaces: []string{model2.CodeResource.Namespace},
			Resources:  []string{model2.CodeResource.Name},
		},
		Order:    90,
		Sync:     true,
		Internal: true,
	})
}

func (m module) initFunctionListeners() {
	m.backendEventHandler.RegisterHandler(backend_event_handler.Handler{
		Id:   "nano-function-listener",
		Name: "nano-function-listener",
		Fn:   m.functionListenerHandler,
		Selector: &model.EventSelector{
			Actions: []model.Event_Action{
				model.Event_CREATE, model.Event_UPDATE, model.Event_DELETE,
			},
			Namespaces: []string{model2.FunctionResource.Namespace},
			Resources:  []string{model2.FunctionResource.Name},
		},
		Order:    90,
		Sync:     true,
		Internal: true,
	})
}

func (m module) initScriptListeners() {
	m.backendEventHandler.RegisterHandler(backend_event_handler.Handler{
		Id:   "nano-script-listener",
		Name: "nano-script-listener",
		Fn:   m.scriptListenerHandler,
		Selector: &model.EventSelector{
			Actions: []model.Event_Action{
				model.Event_CREATE,
			},
			Namespaces: []string{model2.ScriptResource.Namespace},
			Resources:  []string{model2.ScriptResource.Name},
		},
		Order:     90,
		Sync:      true,
		Internal:  true,
		Finalizes: true,
	})
}

func (m module) initExistingCodes() {
	var codeRecords, _, err = m.container.GetRecordService().List(util.SystemContext, service.RecordListParams{
		Namespace: model2.CodeResource.Namespace,
		Resource:  model2.CodeResource.Name,
		Limit:     1000000,
	})

	if err != nil {
		log.Panic(err)
	}

	for _, codeRecord := range codeRecords {
		var code = model2.CodeMapperInstance.FromRecord(codeRecord)

		err := m.codeExecutor.registerCode(code)

		if err != nil {
			logrus.WithField("CodeName", code.Name).Error(err)
		}
	}
}

func (m module) initExistingFunctions() {
	var functionRecords, _, err = m.container.GetRecordService().List(util.SystemContext, service.RecordListParams{
		Namespace: model2.FunctionResource.Namespace,
		Resource:  model2.FunctionResource.Name,
		Limit:     1000000,
	})

	if err != nil {
		log.Panic(err)
	}

	for _, functionRecord := range functionRecords {
		var function = model2.FunctionMapperInstance.FromRecord(functionRecord)

		err := m.codeExecutor.registerFunction(function)

		if err != nil {
			logrus.WithField("CodeName", function.Name).Error(err)
		}
	}
}

func (m module) scriptListenerHandler(ctx context.Context, event *model.Event) (*model.Event, errors.ServiceError) {
	for _, record := range event.Records {
		script := model2.ScriptMapperInstance.FromRecord(record)

		switch event.Action {
		case model.Event_CREATE:
			output, err := m.codeExecutor.runScript(ctx, script)

			if output != nil {
				st, err := structpb.NewValue(output)

				if err != nil {
					return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
				}

				record.Properties["output"] = st
			}

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		}
	}

	return event, nil
}

func (m module) codeListenerHandler(ctx context.Context, event *model.Event) (*model.Event, errors.ServiceError) {
	for _, record := range event.Records {
		code := model2.CodeMapperInstance.FromRecord(record)

		switch event.Action {
		case model.Event_CREATE:
			err := m.codeExecutor.registerCode(code)

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		case model.Event_UPDATE:
			err := m.codeExecutor.updateCode(code)

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		case model.Event_DELETE:
			err := m.codeExecutor.unRegisterCode(code)

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		}
	}

	return event, nil
}

func (m module) functionListenerHandler(ctx context.Context, event *model.Event) (*model.Event, errors.ServiceError) {
	for _, record := range event.Records {
		function := model2.FunctionMapperInstance.FromRecord(record)

		switch event.Action {
		case model.Event_CREATE:
			err := m.codeExecutor.registerFunction(function)

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		case model.Event_UPDATE:
			err := m.codeExecutor.updateFunction(function)

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		case model.Event_DELETE:
			err := m.codeExecutor.unRegisterFunction(function)

			if err != nil {
				return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
			}
		}
	}

	return event, nil
}

func NewModule(container service.Container) service.Module {
	backendEventHandler := container.GetBackendEventHandler().(backend_event_handler.BackendEventHandler)
	return &module{container: container, codeExecutor: newCodeExecutorService(container, backendEventHandler), backendEventHandler: backendEventHandler}
}
