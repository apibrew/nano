package test

import (
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/test/setup"
	"github.com/apibrew/nano/pkg/model"
	"testing"
)

func TestNanoScriptBasic(t *testing.T) {
	var api = api.NewInterface(container)

	var testFn = new(model.Script)
	testFn.Source = `return 5 + 6`

	_, err := api.Apply(setup.Ctx, model.ScriptMapperInstance.ToUnstructured(testFn))

	if err != nil {
		t.Error(err)
		return
	}

}
