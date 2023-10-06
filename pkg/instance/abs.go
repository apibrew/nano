package instance

import (
	"faas/pkg/model"
	"github.com/apibrew/apibrew/pkg/client"
	"github.com/apibrew/apibrew/pkg/resource_model"
	"sync"
)

type InstanceClient interface {
	Init() error
}

type instanceClient struct {
	client                            client.Client
	extensionRepository               client.Repository[*resource_model.Extension]
	functionRepository                client.Repository[*model.Function]
	functionExecutionEngineRepository client.Repository[*model.FunctionExecutionEngine]
	functionTriggerRepository         client.Repository[*model.FunctionTrigger]
	resourceRuleRepository            client.Repository[*model.ResourceRule]
	ext                               client.Extension
	l                                 sync.Locker
	functions                         []*model.Function
	functionExecutionEngines          []*model.FunctionExecutionEngine
	functionTriggers                  []*model.FunctionTrigger
	resourceRules                     []*model.ResourceRule
}

func (i *instanceClient) Init() error {
	err := i.RegisterExtensions()

	if err != nil {
		return err
	}

	err = i.registerPoll(err)

	if err != nil {
		return err
	}

	i.loadAll()

	return nil
}

func NewInstanceClient(config client.ServerConfig) (InstanceClient, error) {
	cl, err := client.NewClientWithConfigServer(config)

	if err != nil {
		return nil, err
	}

	return &instanceClient{
		client:                            cl,
		extensionRepository:               client.NewRepository[*resource_model.Extension](cl, resource_model.ExtensionMapperInstance),
		functionRepository:                client.NewRepository[*model.Function](cl, model.FunctionMapperInstance),
		functionExecutionEngineRepository: client.NewRepository[*model.FunctionExecutionEngine](cl, model.FunctionExecutionEngineMapperInstance),
		functionTriggerRepository:         client.NewRepository[*model.FunctionTrigger](cl, model.FunctionTriggerMapperInstance),
		resourceRuleRepository:            client.NewRepository[*model.ResourceRule](cl, model.ResourceRuleMapperInstance),
		ext:                               cl.NewPollExtension(),
		l:                                 new(sync.Mutex),
	}, nil
}
