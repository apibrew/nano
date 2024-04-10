package nano

//go:generate apbr generate --platform=golang --path=./model/ --package=model --source-file=schema.yml
//go:generate statik -src=./builtin -ns=nano-builtin -dest=.
