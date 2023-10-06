package instance

import (
	"context"
	"faas/pkg/model"
	model2 "github.com/apibrew/apibrew/pkg/model"
)

func (i *instanceClient) registerPoll(err error) error {
	syncChan, err := i.client.PollEvents(context.TODO(), FaasChannelSync)

	if err != nil {
		return err
	}

	go func() {
		for event := range syncChan {
			i.handleSyncEvent(event)
		}
	}()

	return nil
}

func (i *instanceClient) handleSyncEvent(event *model2.Event) {
	switch event.Resource.Name {
	case model.FunctionMapperInstance.ResourceIdentity().Name:
		go i.loadFunctions()
	case model.FunctionExecutionEngineMapperInstance.ResourceIdentity().Name:
		go i.loadFunctionExecutionEngines()
	case model.FunctionTriggerMapperInstance.ResourceIdentity().Name:
		go i.loadFunctionTriggers()
	case model.ResourceRuleMapperInstance.ResourceIdentity().Name:
		go i.loadResourceRules()
	case model.LambdaMapperInstance.ResourceIdentity().Name:
		go i.loadLambdas()
	}
}
