package test

import (
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/formats/unstructured"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/apibrew/nano/pkg/model"
	"testing"
)

func runScript(t testing.TB, source string) unstructured.Unstructured {
	var api = api.NewInterface(container)

	var testFn = new(model.Script)
	testFn.Source = source
	testFn.Language = model.ScriptLanguage_JAVASCRIPT
	testFn.ContentFormat = model.ScriptContentFormat_TEXT

	result, err := api.Apply(util.SystemContext, model.ScriptMapperInstance.ToUnstructured(testFn))

	if err != nil {
		t.Error(err)
		return nil
	}
	return result
}
