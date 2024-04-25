package abs

import (
	"context"
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/service"
	backend_event_handler "github.com/apibrew/apibrew/pkg/service/backend-event-handler"
	"github.com/apibrew/nano/pkg/util"
)

type GlobalObject interface {
	Define(name string, value interface{})
	Get(name string) interface{}
}

type VmOptions struct {
}

type CodeExecutorService interface {
	RunInlineScript(ctx context.Context, identifier string, source string) (result any, err error)
	GetContainer() service.Container
	GetBackendEventHandler() backend_event_handler.BackendEventHandler
	GetGlobalObject() GlobalObject
}

type EventWithContextSignal struct {
	ProcessedEvent *model.Event
	Err            error
}

type EventWithContext struct {
	Ctx    context.Context
	Event  *model.Event
	Signal chan EventWithContextSignal
}

type HandlerData struct {
	Ch chan *EventWithContext
}

type CodeExecutionContext interface {
	HandlerMap() util.Map[string, *HandlerData]
	Context() context.Context
	GetCodeIdentifier() string
	IsScriptMode() bool
	RegisterRevert(f func() error)
	TransactionalEnabled() bool
	BeginTransaction() error
	CommitTransaction() error
	RollbackTransaction() error
}
