package nano

import (
	"context"
	"github.com/dop251/goja"
	log "github.com/sirupsen/logrus"
)

type codeExecutionContext struct {
	handlerIds             []string
	closeHandlers          []func()
	ctx                    context.Context
	vm                     *goja.Runtime
	identifier             string
	scriptMode             bool
	insideTransaction      bool
	transactionRollbackBag []func() error
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

func (c *codeExecutionContext) AddHandlerId(id string) {
	c.handlerIds = append(c.handlerIds, id)
}

func (c *codeExecutionContext) RemoveHandlerId(id string) {
	for i, handlerId := range c.handlerIds {
		if handlerId == id {
			c.handlerIds = append(c.handlerIds[:i], c.handlerIds[i+1:]...)
			return
		}
	}
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
