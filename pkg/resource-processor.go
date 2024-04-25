package nano

import (
	"context"
	"fmt"
	"github.com/apibrew/apibrew/pkg/errors"
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/service"
	backend_event_handler "github.com/apibrew/apibrew/pkg/service/backend-event-handler"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/sirupsen/logrus"
)

type ResourceProcessor[T any] interface {
	MapperTo(record *model.Record) T

	Register(entity T) error
	Update(entity T) error
	UnRegister(entity T) error
}

func RegisterResourceProcessor[T any](handlerName string,
	processor ResourceProcessor[T],
	backendEventHandler backend_event_handler.BackendEventHandler,
	container service.Container,
	resource *model.Resource) error {
	handler := func(ctx context.Context, event *model.Event) (*model.Event, error) {
		for _, record := range event.Records {
			entity := processor.MapperTo(record)

			switch event.Action {
			case model.Event_CREATE:
				err := processor.Register(entity)

				if err != nil {
					return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
				}
			case model.Event_UPDATE:
				err := processor.Update(entity)

				if err != nil {
					return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
				}
			case model.Event_DELETE:
				err := processor.UnRegister(entity)

				if err != nil {
					return nil, errors.RecordValidationError.WithMessage(fmt.Sprintf("%v", err))
				}
			}
		}

		return event, nil
	}

	backendEventHandler.RegisterHandler(backend_event_handler.Handler{
		Id:   handlerName,
		Name: handlerName,
		Fn:   handler,
		Selector: &model.EventSelector{
			Actions: []model.Event_Action{
				model.Event_CREATE, model.Event_UPDATE, model.Event_DELETE,
			},
			Namespaces: []string{resource.Namespace},
			Resources:  []string{resource.Name},
		},
		Order:    90,
		Sync:     true,
		Internal: true,
	})

	var codeRecords, _, err = container.GetRecordService().List(util.SystemContext, service.RecordListParams{
		Namespace: resource.Namespace,
		Resource:  resource.Name,
		Limit:     1000000,
	})

	if err != nil {
		return err
	}

	for _, record := range codeRecords {
		entity := processor.MapperTo(record)

		if err != nil {
			return err
		}

		err := processor.Register(entity)

		if err != nil {
			logrus.Error(err)
		}
	}

	return nil
}
