// Code generated by apbr generate. DO NOT EDIT.
// versions:
// 	apbr generate v1.2

//go:build !codeanalysis

package model

import (
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/util"
	"google.golang.org/protobuf/types/known/structpb"
)

var CodeResource = &model.Resource{
	Name:        "Code",
	Namespace:   "nano",
	Title:       util.Pointer("Code"),
	Description: util.Pointer("Nano code"),
	Types: []*model.ResourceSubType{
		{
			Name:        "AuditData",
			Title:       "Audit Data",
			Description: "Audit Data is a type that represents the audit data of a resource/record. ",
			Properties: []*model.ResourceProperty{
				{
					Name:         "createdBy",
					Type:         model.ResourceProperty_STRING,
					Length:       256,
					Immutable:    true,
					ExampleValue: structpb.NewStringValue("admin"),

					Annotations: map[string]string{
						"SpecialProperty": "true",
					},
				},
				{
					Name:         "updatedBy",
					Type:         model.ResourceProperty_STRING,
					Length:       256,
					ExampleValue: structpb.NewStringValue("admin"),

					Annotations: map[string]string{
						"SpecialProperty": "true",
					},
				},
				{
					Name:         "createdOn",
					Type:         model.ResourceProperty_TIMESTAMP,
					Immutable:    true,
					ExampleValue: structpb.NewStringValue("2024-04-29T01:58:23+04:00"),

					Annotations: map[string]string{
						"SpecialProperty": "true",
					},
				},
				{
					Name:         "updatedOn",
					Type:         model.ResourceProperty_TIMESTAMP,
					ExampleValue: structpb.NewStringValue("2024-04-29T01:58:23+04:00"),

					Annotations: map[string]string{
						"SpecialProperty": "true",
					},
				},
			},

			Annotations: map[string]string{
				"EnableAudit":  "true",
				"OpenApiGroup": "meta",
			},
		},
	},
	Properties: []*model.ResourceProperty{
		{
			Name:         "id",
			Type:         model.ResourceProperty_UUID,
			Primary:      true,
			Required:     true,
			Immutable:    true,
			ExampleValue: structpb.NewStringValue("a39621a4-6d48-11ee-b962-0242ac120002"),

			Annotations: map[string]string{
				"SpecialProperty": "true",
			},
		},
		{
			Name:     "content",
			Type:     model.ResourceProperty_STRING,
			Length:   64000,
			Required: true,

			Annotations: map[string]string{
				"SQLType": "TEXT",
			},
		},
		{
			Name:         "contentFormat",
			Type:         model.ResourceProperty_ENUM,
			Required:     true,
			DefaultValue: structpb.NewStringValue("TEXT"),
			EnumValues:   []string{"TEXT", "TAR", "TAR_GZ"},
		},
		{
			Name:         "concurrencyLevel",
			Type:         model.ResourceProperty_INT32,
			DefaultValue: structpb.NewNumberValue(8),
		},
		{
			Name: "annotations",
			Type: model.ResourceProperty_MAP,
			Item: &model.ResourceProperty{
				Name: "",
				Type: model.ResourceProperty_STRING,
			},
		},
		{
			Name:      "name",
			Type:      model.ResourceProperty_STRING,
			Length:    255,
			Required:  true,
			Unique:    true,
			Immutable: true,
		},
		{
			Name:         "language",
			Type:         model.ResourceProperty_ENUM,
			Required:     true,
			DefaultValue: structpb.NewStringValue("JAVASCRIPT"),
			EnumValues:   []string{"JAVASCRIPT", "TYPESCRIPT"},
		},
		{
			Name:         "version",
			Type:         model.ResourceProperty_INT32,
			Required:     true,
			DefaultValue: structpb.NewNumberValue(1),
			ExampleValue: structpb.NewNumberValue(1),

			Annotations: map[string]string{
				"SpecialProperty":     "true",
				"AllowEmptyPrimitive": "true",
			},
		},
		{
			Name:         "auditData",
			Type:         model.ResourceProperty_STRUCT,
			TypeRef:      util.Pointer("AuditData"),
			ExampleValue: structpb.NewStructValue(&structpb.Struct{Fields: map[string]*structpb.Value{"createdBy": structpb.NewStringValue("admin"), "updatedBy": structpb.NewStringValue("admin"), "createdOn": structpb.NewStringValue("2024-04-29T01:58:23+04:00"), "updatedOn": structpb.NewStringValue("2024-04-29T01:58:23+04:00")}}),

			Annotations: map[string]string{
				"SpecialProperty": "true",
			},
		},
	},

	Annotations: map[string]string{
		"EnableAudit":  "true",
		"OpenApiGroup": "meta",
	},
}
