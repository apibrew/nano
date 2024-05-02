package nano

import (
	"context"
	"github.com/apibrew/apibrew/pkg/model"
	model2 "github.com/apibrew/nano/pkg/model"
)

type codeProcessor struct {
	codeExecutor *codeExecutorService
}

func (f codeProcessor) MapperTo(record *model.Record) *model2.Code {
	return model2.CodeMapperInstance.FromRecord(record)
}

func (f codeProcessor) Register(ctx context.Context, entity *model2.Code) error {
	return f.codeExecutor.registerCode(ctx, entity)
}

func (f codeProcessor) Update(ctx context.Context, entity *model2.Code) error {
	return f.codeExecutor.updateCode(ctx, entity)
}

func (f codeProcessor) UnRegister(ctx context.Context, entity *model2.Code) error {
	return f.codeExecutor.unRegisterCode(ctx, entity)
}
