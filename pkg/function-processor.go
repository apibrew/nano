package nano

import (
	"github.com/apibrew/apibrew/pkg/model"
	model2 "github.com/apibrew/nano/pkg/model"
)

type functionProcessor struct {
	codeExecutor *codeExecutorService
}

func (f functionProcessor) MapperTo(record *model.Record) *model2.Function {
	return model2.FunctionMapperInstance.FromRecord(record)
}

func (f functionProcessor) Register(entity *model2.Function) error {
	return f.codeExecutor.registerFunction(entity)
}

func (f functionProcessor) Update(entity *model2.Function) error {
	return f.codeExecutor.updateFunction(entity)
}

func (f functionProcessor) UnRegister(entity *model2.Function) error {
	return f.codeExecutor.unRegisterFunction(entity)
}
