package nano

import (
	"github.com/apibrew/apibrew/pkg/model"
	model2 "github.com/apibrew/nano/pkg/model"
)

type codeProcessor struct {
	codeExecutor *codeExecutorService
}

func (f codeProcessor) MapperTo(record *model.Record) *model2.Code {
	return model2.CodeMapperInstance.FromRecord(record)
}

func (f codeProcessor) Register(entity *model2.Code) error {
	return f.codeExecutor.registerCode(entity)
}

func (f codeProcessor) Update(entity *model2.Code) error {
	return f.codeExecutor.updateCode(entity)
}

func (f codeProcessor) UnRegister(entity *model2.Code) error {
	return f.codeExecutor.unRegisterCode(entity)
}
