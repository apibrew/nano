package nano

import (
	"context"
	"github.com/apibrew/nano/pkg/abs"
	"github.com/apibrew/nano/pkg/util"
	"github.com/dop251/goja"
	log "github.com/sirupsen/logrus"
)

type codeExecutionContext struct {
	ctx                    context.Context
	cancel                 context.CancelFunc
	vms                    []*goja.Runtime
	identifier             string
	scriptMode             bool
	insideTransaction      bool
	transactionRollbackBag []func() error
	handlerMap             util.Map[string, *abs.HandlerData]
}

func (c *codeExecutionContext) HandlerMap() util.Map[string, *abs.HandlerData] {
	return c.handlerMap
}

func (c *codeExecutionContext) BeginTransaction() error {
	c.insideTransaction = true
	return nil
}

func (c *codeExecutionContext) CommitTransaction() error {
	c.insideTransaction = false
	c.transactionRollbackBag = nil
	return nil
}

func (c *codeExecutionContext) RollbackTransaction() error {
	for _, f := range c.transactionRollbackBag {
		if err := f(); err != nil {
			log.Error(err)
		}
	}

	c.insideTransaction = false
	c.transactionRollbackBag = nil
	return nil
}

func (c *codeExecutionContext) RegisterRevert(f func() error) {
	c.transactionRollbackBag = append(c.transactionRollbackBag, f)
}

func (c *codeExecutionContext) TransactionalEnabled() bool {
	return c.insideTransaction
}

func (c *codeExecutionContext) Context() context.Context {
	return c.ctx
}

func (c *codeExecutionContext) GetCodeIdentifier() string {
	return c.identifier
}

func (c *codeExecutionContext) IsScriptMode() bool {
	return c.scriptMode
}
