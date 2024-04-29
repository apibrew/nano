package nano

import (
	"github.com/apibrew/apibrew/pkg/model"
	model2 "github.com/apibrew/nano/pkg/model"
)

type moduleProcessor struct {
	codeExecutor *codeExecutorService
}

func (f moduleProcessor) MapperTo(record *model.Record) *model2.Module {
	return model2.ModuleMapperInstance.FromRecord(record)
}

func (f moduleProcessor) Register(entity *model2.Module) error {
	return f.codeExecutor.registerModule(entity)
}

func (f moduleProcessor) Update(entity *model2.Module) error {
	return f.codeExecutor.updateModule(entity)
}

func (f moduleProcessor) UnRegister(entity *model2.Module) error {
	return f.codeExecutor.unRegisterModule(entity)
}
