package instance

import (
	"context"
	"faas/pkg/model"
	"github.com/apibrew/apibrew/pkg/resource_model"
	"github.com/apibrew/apibrew/pkg/util"
	log "github.com/sirupsen/logrus"
)

func (i *instanceClient) RegisterExtensions() error {
	log.Info("Registering extensions")
	extensions := i.prepareExtensions()
	log.Info("Extensions prepared")

	ctx := context.TODO()

	for _, extension := range extensions {
		log.Info("Applying extension: " + extension.Name)
		_, err := i.extensionRepository.Apply(ctx, extension)

		if err != nil {
			return err
		}
		log.Info("Extension applied: " + extension.Name)
	}

	return nil
}

func (i *instanceClient) prepareExtensions() []*resource_model.Extension {
	var list []*resource_model.Extension
	// function extension
	syncDataExtension := &resource_model.Extension{
		Name:        FassExtensionsPrefix + "sync",
		Description: util.Pointer("Function extension for FaaS"),
		Selector: &resource_model.ExtensionEventSelector{
			Actions: []resource_model.EventAction{
				resource_model.EventAction_CREATE,
				resource_model.EventAction_UPDATE,
				resource_model.EventAction_DELETE,
			},
			Namespaces: []string{"logic"},
			Resources: []string{
				model.FunctionMapperInstance.ResourceIdentity().Name,
				model.FunctionExecutionEngineMapperInstance.ResourceIdentity().Name,
				model.FunctionTriggerMapperInstance.ResourceIdentity().Name,
				model.ResourceRuleMapperInstance.ResourceIdentity().Name,
				model.LambdaMapperInstance.ResourceIdentity().Name,
			},
			Ids:         nil,
			Annotations: nil,
		},
		Order: 300,
		Sync:  false,
		Call: resource_model.ExtensionExternalCall{
			ChannelCall: &resource_model.ExtensionChannelCall{
				ChannelKey: FaasChannelSync,
			},
		},
	}

	functionExecutionExtension := &resource_model.Extension{
		Name:        FassExtensionsPrefix + "function-execution",
		Description: util.Pointer("Function extension for FaaS"),
		Selector: &resource_model.ExtensionEventSelector{
			Actions: []resource_model.EventAction{
				resource_model.EventAction_CREATE,
			},
			Namespaces: []string{"logic"},
			Resources: []string{
				model.FunctionExecutionMapperInstance.ResourceIdentity().Name,
			},
			Ids:         nil,
			Annotations: nil,
		},
		Order:     1,
		Sync:      true,
		Responds:  true,
		Finalizes: true,
		Call: resource_model.ExtensionExternalCall{
			ChannelCall: &resource_model.ExtensionChannelCall{
				ChannelKey: FaasChannelExec,
			},
		},
	}

	list = append(list, syncDataExtension, functionExecutionExtension)

	return list
}
