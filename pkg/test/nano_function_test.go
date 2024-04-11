package test

import (
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/apibrew/nano/pkg/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestNanoFunction(t *testing.T) {
	var api = api.NewInterface(container)

	var testFn = new(model.Function)
	testFn.Name = "add"
	testFn.Source = `
return a + b
`
	testFn.Language = model.FunctionLanguage_JAVASCRIPT
	testFn.ContentFormat = model.FunctionContentFormat_TEXT

	_, err := api.Apply(util.SystemContext, model.FunctionMapperInstance.ToUnstructured(testFn))

	if err != nil {
		t.Error(err)
		return
	}

	var checkScript = new(model.Script)
	checkScript.Source = `add(5, 6)`
	checkScript.Language = model.ScriptLanguage_JAVASCRIPT
	checkScript.ContentFormat = model.ScriptContentFormat_TEXT

	result, err := api.Apply(util.SystemContext, model.ScriptMapperInstance.ToUnstructured(checkScript))

	if err != nil {
		t.Error(err)
		return
	}

	assert.NotNil(t, result["output"])

	if t.Failed() {
		return
	}

	output := result["output"]

	assert.Equal(t, float64(11), output)
}

func TestNanoFunctionAddingToExistingCodes(t *testing.T) {
	var api = api.NewInterface(container)

	var testFn2 = new(model.Function)
	testFn2.Name = "callMe"
	testFn2.Source = `
return a(b, c)
`
	testFn2.Language = model.FunctionLanguage_JAVASCRIPT
	testFn2.ContentFormat = model.FunctionContentFormat_TEXT

	_, err := api.Apply(util.SystemContext, model.FunctionMapperInstance.ToUnstructured(testFn2))

	if err != nil {
		t.Error(err)
		return
	}

	var testFn = new(model.Function)
	testFn.Name = "add"
	testFn.Source = `
	return a + b
	`
	testFn.Language = model.FunctionLanguage_JAVASCRIPT
	testFn.ContentFormat = model.FunctionContentFormat_TEXT

	_, err = api.Apply(util.SystemContext, model.FunctionMapperInstance.ToUnstructured(testFn))

	if err != nil {
		t.Error(err)
		return
	}

	var checkScript = new(model.Script)
	checkScript.Source = `callMe(add, 5, 6)`
	checkScript.Language = model.ScriptLanguage_JAVASCRIPT
	checkScript.ContentFormat = model.ScriptContentFormat_TEXT

	result, err := api.Apply(util.SystemContext, model.ScriptMapperInstance.ToUnstructured(checkScript))

	if err != nil {
		t.Error(err)
		return
	}

	assert.NotNil(t, result["output"])

	if t.Failed() {
		return
	}

	output := result["output"]

	assert.Equal(t, float64(11), output)
}
