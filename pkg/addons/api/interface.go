package api

import (
	"github.com/apibrew/apibrew/pkg/api"
	"github.com/apibrew/apibrew/pkg/formats/unstructured"
	"github.com/apibrew/apibrew/pkg/resource_model"
	"github.com/apibrew/apibrew/pkg/util"
	"github.com/apibrew/nano/pkg/abs"
	util2 "github.com/apibrew/nano/pkg/addons/util"
	"github.com/dop251/goja"
)

func create(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(record unstructured.Unstructured) unstructured.Unstructured {
	return func(record unstructured.Unstructured) unstructured.Unstructured {
		result, err := apiInterface.Create(cec.Context(), record)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}

		return result
	}
}

func update(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(record unstructured.Unstructured) unstructured.Unstructured {
	return func(record unstructured.Unstructured) unstructured.Unstructured {
		result, err := apiInterface.Update(cec.Context(), record)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}

		return result
	}
}

func apply(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(record unstructured.Unstructured) unstructured.Unstructured {
	return func(record unstructured.Unstructured) unstructured.Unstructured {
		result, err := apiInterface.Apply(cec.Context(), record)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}

		return result
	}
}

func delete_(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(record unstructured.Unstructured) {
	return func(record unstructured.Unstructured) {
		err := apiInterface.Delete(cec.Context(), record)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}
	}
}

func load(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(record unstructured.Unstructured, params api.LoadParams) unstructured.Unstructured {
	return func(record unstructured.Unstructured, params api.LoadParams) unstructured.Unstructured {
		result, err := apiInterface.Load(cec.Context(), record, params)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}

		return result
	}
}

func list(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(params api.ListParams) unstructured.Unstructured {
	return func(params api.ListParams) unstructured.Unstructured {
		result, err := apiInterface.List(cec.Context(), params)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}

		return unstructured.Unstructured{
			"content": util.ArrayMap(result.Content, func(item unstructured.Unstructured) interface{} {
				return item
			}),
			"total": result.Total,
		}
	}
}

func resourceByName(cec abs.CodeExecutionContext, vm *goja.Runtime, apiInterface api.Interface) func(typeName string) *resource_model.Resource {
	return func(typeName string) *resource_model.Resource {
		result, err := apiInterface.GetResourceByType(cec.Context(), typeName)

		if err != nil {
			util2.ThrowError(vm, err.Error())
		}

		return result
	}
}
