package nano

import (
	"context"
	"github.com/dop251/goja"
)

type codeExecutionContext struct {
	handlerIds    []string
	closeHandlers []func()
	ctx           context.Context
	vm            *goja.Runtime
	identifier    string
	scriptMode    bool
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
