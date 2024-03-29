package addons

import (
	"github.com/apibrew/apibrew/pkg/service"
	"github.com/apibrew/nano/pkg/abs"
	"github.com/apibrew/nano/pkg/addons/api"
	"github.com/apibrew/nano/pkg/addons/auth"
	"github.com/apibrew/nano/pkg/addons/aws"
	"github.com/apibrew/nano/pkg/addons/console"
	"github.com/apibrew/nano/pkg/addons/execute"
	"github.com/apibrew/nano/pkg/addons/global"
	"github.com/apibrew/nano/pkg/addons/http"
	"github.com/apibrew/nano/pkg/addons/mail"
	"github.com/apibrew/nano/pkg/addons/resource"
	"github.com/dop251/goja"
)

func Register(vm *goja.Runtime, cec abs.CodeExecutionContext, s abs.CodeExecutorService, codeName string, container service.Container) error {
	if err := console.Register(vm, codeName); err != nil {
		return err
	}

	if err := resource.Register(vm, cec, s); err != nil {
		return err
	}

	if err := mail.Register(vm); err != nil {
		return err
	}

	if err := http.Register(vm); err != nil {
		return err
	}

	if err := auth.Register(vm, container); err != nil {
		return err
	}

	if err := execute.Register(vm, s); err != nil {
		return err
	}

	if err := global.Register(vm, s); err != nil {
		return err
	}

	if err := aws.Register(vm); err != nil {
		return err
	}

	if err := api.Register(vm, cec, container); err != nil {
		return err
	}

	return nil
}
