package instance

import (
	"context"
	"faas/pkg/model"
	"github.com/apibrew/apibrew/pkg/client"
	"github.com/apibrew/apibrew/pkg/errors"
	"github.com/apibrew/apibrew/pkg/util"
	log "github.com/sirupsen/logrus"
)

func (i *instanceClient) loadFunctions() {
	i.l.Lock()
	defer i.l.Unlock()

	functions, _, err := i.functionRepository.Find(context.TODO(), client.FindParams{
		Limit: 10000,
	})

	if err != nil {
		log.Error(err)
		return
	}

	_ = util.ArrayDiffer(i.functions, functions, func(a, b *model.Function) bool {
		return a.Name == b.Name
	}, func(a, b *model.Function) bool {
		return a.Name == b.Name && a.Version == b.Version
	}, func(rec *model.Function) errors.ServiceError {
		// new function
		go i.registerFunction(rec)
		return nil
	}, func(e, u *model.Function) errors.ServiceError {
		// updated function
		go i.updateFunction(u)
		return nil
	}, func(rec *model.Function) errors.ServiceError {
		// deleted function
		go i.unRegisterFunction(rec)
		return nil
	})

	i.functions = functions
}

func (i *instanceClient) loadFunctionExecutionEngines() {
	i.l.Lock()
	defer i.l.Unlock()

	functionExecutionEngines, _, err := i.functionExecutionEngineRepository.Find(context.TODO(), client.FindParams{
		Limit: 10000,
	})

	if err != nil {
		log.Error(err)
		return
	}

	i.functionExecutionEngines = functionExecutionEngines
}

func (i *instanceClient) loadFunctionTriggers() {
	i.l.Lock()
	defer i.l.Unlock()

	functionTriggers, _, err := i.functionTriggerRepository.Find(context.TODO(), client.FindParams{
		Limit: 10000,
	})

	if err != nil {
		log.Error(err)
		return
	}

	i.functionTriggers = functionTriggers
}

func (i *instanceClient) loadResourceRules() {
	i.l.Lock()
	defer i.l.Unlock()

	resourceRules, _, err := i.resourceRuleRepository.Find(context.TODO(), client.FindParams{
		Limit: 10000,
	})

	if err != nil {
		log.Error(err)
		return
	}

	i.resourceRules = resourceRules
}

func (i *instanceClient) loadLambdas() {
	i.l.Lock()
	defer i.l.Unlock()

	lambdas, _, err := i.resourceRuleRepository.Find(context.TODO(), client.FindParams{
		Limit: 10000,
	})

	if err != nil {
		log.Error(err)
		return
	}

	i.resourceRules = lambdas
}

func (i *instanceClient) loadAll() {
	i.loadFunctions()
	i.loadFunctionExecutionEngines()
	i.loadFunctionTriggers()
	i.loadResourceRules()
	i.loadLambdas()
}
