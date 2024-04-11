package nano

import (
	"context"
	"fmt"
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/errors"
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/resources"
	"github.com/apibrew/apibrew/pkg/service"
	backend_event_handler "github.com/apibrew/apibrew/pkg/service/backend-event-handler"
	"github.com/apibrew/apibrew/pkg/util"
	model2 "github.com/apibrew/nano/pkg/model"
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
	m.initScriptListeners()

	if err := RegisterResourceProcessor[*model2.Function](
		"nano-function-listener",
		&functionProcessor{
			codeExecutor: m.codeExecutor,
		},
		m.backendEventHandler,
		m.container,
		model2.FunctionResource,
	); err != nil {
		log.Panic(err)
	}

	if err := RegisterResourceProcessor[*model2.Code](
		"nano-code-listener",
		&codeProcessor{
			codeExecutor: m.codeExecutor,
		},
		m.backendEventHandler,
		m.container,
		model2.CodeResource,
	); err != nil {
		log.Panic(err)
	}

	if err := RegisterResourceProcessor[*model2.CronJob](
		"nano-cron-job-listener",
		&cronJobProcessor{
			codeExecutor: m.codeExecutor,
			api:          api.NewInterface(m.container),
		},
		m.backendEventHandler,
		m.container,
		model2.CronJobResource,
	); err != nil {
		log.Panic(err)
	}
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

func NewModule(container service.Container) service.Module {
	backendEventHandler := container.GetBackendEventHandler().(backend_event_handler.BackendEventHandler)
	return &module{container: container, codeExecutor: newCodeExecutorService(container, backendEventHandler), backendEventHandler: backendEventHandler}
}
