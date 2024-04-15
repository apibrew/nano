package auth

import (
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/service"
	"github.com/apibrew/apibrew/pkg/util"
	util2 "github.com/apibrew/nano/pkg/addons/util"
	"github.com/dop251/goja"
)

type authObject struct {
	container service.Container
	vm        *goja.Runtime
}

func (c *authObject) Authenticate(username string, password string, term string) interface{} {
	token, err := c.container.GetAuthenticationService().Authenticate(util.SystemContext, username, password, model.TokenTerm(model.TokenTerm_value[term]), false)

	if err != nil {
		util2.ThrowError(c.vm, err.Error())
	}

	return token.Content
}

func (c *authObject) AuthenticateWithoutPassword(username string, term string) interface{} {
	token, err := c.container.GetAuthenticationService().AuthenticateWithoutPassword(util.SystemContext, username, model.TokenTerm(model.TokenTerm_value[term]))

	if err != nil {
		util2.ThrowError(c.vm, err.Error())
	}

	return token.Content
}

func Register(vm *goja.Runtime, container service.Container) error {
	obj := &authObject{vm: vm, container: container}
	return vm.Set("auth", obj)
}
