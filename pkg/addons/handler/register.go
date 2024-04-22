package handler

import (
	"github.com/apibrew/nano/pkg/abs"
	"github.com/dop251/goja"
)

func Register(vm *goja.Runtime, cec abs.CodeExecutionContext, s abs.CodeExecutorService) error {
	return vm.Set("handle", handle(cec, s.GetBackendEventHandler()))
}
