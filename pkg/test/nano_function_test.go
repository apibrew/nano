package test

import (
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/test/setup"
	"github.com/apibrew/nano/pkg/model"
	"testing"
)

func TestNanoFunction(t *testing.T) {
	var api = api.NewInterface(container)

	var testFn = new(model.Function)
	testFn.Name = "test"

	_, err := api.Apply(setup.Ctx, model.FunctionMapperInstance.ToUnstructured(testFn))

	if err != nil {
		t.Error(err)
		return
	}

}
