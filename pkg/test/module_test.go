package test

import (
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/apibrew/nano/pkg/model"
	"github.com/stretchr/testify/assert"
	"testing"
)

func TestBasicModule(t *testing.T) {
	var api = api.NewInterface(container)

	var module1 = new(model.Module)
	module1.Name = `module1`
	module1.Source = `
		exports.add = function(a, b) {
			return a + b;
		}
`
	module1.Language = model.ModuleLanguage_JAVASCRIPT
	module1.ContentFormat = model.ModuleContentFormat_TEXT

	_, err := api.Apply(util.SystemContext, model.ModuleMapperInstance.ToUnstructured(module1))

	if err != nil {
		t.Error(err)
		return
	}

	result := runScript(t, `
		const module1 = require('module1');

		module1.add(5, 6);
`)
	if t.Failed() {
		return
	}

	assert.NotNil(t, result["output"])

	if t.Failed() {
		return
	}

	output := result["output"]

	assert.Equal(t, float64(11), output)
}

func TestBasicModuleTypescript(t *testing.T) {
	var api = api.NewInterface(container)

	var module1 = new(model.Module)
	module1.Name = `module1`
	module1.Source = `
		export function add(a: number, b: number): number {
			return a + b;
		}
`
	module1.Language = model.ModuleLanguage_TYPESCRIPT
	module1.ContentFormat = model.ModuleContentFormat_TEXT

	_, err := api.Apply(util.SystemContext, model.ModuleMapperInstance.ToUnstructured(module1))

	if err != nil {
		t.Error(err)
		return
	}

	result := runScriptWithLanguage(t, `
		import {add} from 'module1';

		dd(5, 6);
`, model.ScriptLanguage_TYPESCRIPT)
	if t.Failed() {
		return
	}

	assert.NotNil(t, result["output"])

	if t.Failed() {
		return
	}

	output := result["output"]

	assert.Equal(t, float64(11), output)
}
