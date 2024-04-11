// Code generated by apbr generate. DO NOT EDIT.
// versions:
// 	apbr generate v1.2

//go:build !codeanalysis

package model

import (
	"github.com/apibrew/apibrew/pkg/abs"
	"github.com/apibrew/apibrew/pkg/formats/unstructured"
	"github.com/apibrew/apibrew/pkg/model"
	"github.com/apibrew/apibrew/pkg/types"
	"google.golang.org/protobuf/types/known/structpb"
)

import "github.com/google/uuid"
import "time"

type CodeMapper struct {
}

func NewCodeMapper() *CodeMapper {
	return &CodeMapper{}
}

var CodeMapperInstance = NewCodeMapper()

func (m *CodeMapper) New() *Code {
	return &Code{}
}

func (m *CodeMapper) ResourceIdentity() abs.ResourceIdentity {
	return abs.ResourceIdentity{
		Namespace: "nano",
		Name:      "Code",
	}
}

func (m *CodeMapper) ToRecord(code *Code) *model.Record {
	var rec = &model.Record{}
	rec.Properties = m.ToProperties(code)
	return rec
}

func (m *CodeMapper) FromRecord(record *model.Record) *Code {
	return m.FromProperties(record.Properties)
}

func (m *CodeMapper) ToProperties(code *Code) map[string]*structpb.Value {
	var properties = make(map[string]*structpb.Value)

	var_Id := code.Id

	if var_Id != nil {
		var var_Id_mapped *structpb.Value

		var var_Id_err error
		var_Id_mapped, var_Id_err = types.ByResourcePropertyType(model.ResourceProperty_UUID).Pack(*var_Id)
		if var_Id_err != nil {
			panic(var_Id_err)
		}
		properties["id"] = var_Id_mapped
	}

	var_ContentFormat := code.ContentFormat

	var var_ContentFormat_mapped *structpb.Value

	var var_ContentFormat_err error
	var_ContentFormat_mapped, var_ContentFormat_err = types.ByResourcePropertyType(model.ResourceProperty_ENUM).Pack(string(var_ContentFormat))
	if var_ContentFormat_err != nil {
		panic(var_ContentFormat_err)
	}
	properties["contentFormat"] = var_ContentFormat_mapped

	var_Annotations := code.Annotations

	if var_Annotations != nil {
		var var_Annotations_mapped *structpb.Value

		var var_Annotations_st *structpb.Struct = new(structpb.Struct)
		var_Annotations_st.Fields = make(map[string]*structpb.Value)
		for key, value := range var_Annotations {

			var_1x := value
			var var_1x_mapped *structpb.Value

			var var_1x_err error
			var_1x_mapped, var_1x_err = types.ByResourcePropertyType(model.ResourceProperty_STRING).Pack(var_1x)
			if var_1x_err != nil {
				panic(var_1x_err)
			}

			var_Annotations_st.Fields[key] = var_1x_mapped
		}
		var_Annotations_mapped = structpb.NewStructValue(var_Annotations_st)
		properties["annotations"] = var_Annotations_mapped
	}

	var_Name := code.Name

	var var_Name_mapped *structpb.Value

	var var_Name_err error
	var_Name_mapped, var_Name_err = types.ByResourcePropertyType(model.ResourceProperty_STRING).Pack(var_Name)
	if var_Name_err != nil {
		panic(var_Name_err)
	}
	properties["name"] = var_Name_mapped

	var_Language := code.Language

	var var_Language_mapped *structpb.Value

	var var_Language_err error
	var_Language_mapped, var_Language_err = types.ByResourcePropertyType(model.ResourceProperty_ENUM).Pack(string(var_Language))
	if var_Language_err != nil {
		panic(var_Language_err)
	}
	properties["language"] = var_Language_mapped

	var_Content := code.Content

	var var_Content_mapped *structpb.Value

	var var_Content_err error
	var_Content_mapped, var_Content_err = types.ByResourcePropertyType(model.ResourceProperty_STRING).Pack(var_Content)
	if var_Content_err != nil {
		panic(var_Content_err)
	}
	properties["content"] = var_Content_mapped

	var_Version := code.Version

	var var_Version_mapped *structpb.Value

	var var_Version_err error
	var_Version_mapped, var_Version_err = types.ByResourcePropertyType(model.ResourceProperty_INT32).Pack(var_Version)
	if var_Version_err != nil {
		panic(var_Version_err)
	}
	properties["version"] = var_Version_mapped

	var_AuditData := code.AuditData

	if var_AuditData != nil {
		var var_AuditData_mapped *structpb.Value

		var_AuditData_mapped = structpb.NewStructValue(&structpb.Struct{Fields: CodeAuditDataMapperInstance.ToProperties(var_AuditData)})
		properties["auditData"] = var_AuditData_mapped
	}
	return properties
}

func (m *CodeMapper) FromProperties(properties map[string]*structpb.Value) *Code {
	var s = m.New()
	if properties["id"] != nil && properties["id"].AsInterface() != nil {

		var_Id := properties["id"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_UUID).UnPack(var_Id)

		if err != nil {
			panic(err)
		}

		var_Id_mapped := new(uuid.UUID)
		*var_Id_mapped = val.(uuid.UUID)

		s.Id = var_Id_mapped
	}
	if properties["contentFormat"] != nil && properties["contentFormat"].AsInterface() != nil {

		var_ContentFormat := properties["contentFormat"]
		var_ContentFormat_mapped := (CodeContentFormat)(var_ContentFormat.GetStringValue())

		s.ContentFormat = var_ContentFormat_mapped
	}
	if properties["annotations"] != nil && properties["annotations"].AsInterface() != nil {

		var_Annotations := properties["annotations"]
		var_Annotations_mapped := make(map[string]string)
		for k, v := range var_Annotations.GetStructValue().Fields {

			var_3x := v
			val, err := types.ByResourcePropertyType(model.ResourceProperty_STRING).UnPack(var_3x)

			if err != nil {
				panic(err)
			}

			var_3x_mapped := val.(string)

			var_Annotations_mapped[k] = var_3x_mapped
		}

		s.Annotations = var_Annotations_mapped
	}
	if properties["name"] != nil && properties["name"].AsInterface() != nil {

		var_Name := properties["name"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_STRING).UnPack(var_Name)

		if err != nil {
			panic(err)
		}

		var_Name_mapped := val.(string)

		s.Name = var_Name_mapped
	}
	if properties["language"] != nil && properties["language"].AsInterface() != nil {

		var_Language := properties["language"]
		var_Language_mapped := (CodeLanguage)(var_Language.GetStringValue())

		s.Language = var_Language_mapped
	}
	if properties["content"] != nil && properties["content"].AsInterface() != nil {

		var_Content := properties["content"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_STRING).UnPack(var_Content)

		if err != nil {
			panic(err)
		}

		var_Content_mapped := val.(string)

		s.Content = var_Content_mapped
	}
	if properties["version"] != nil && properties["version"].AsInterface() != nil {

		var_Version := properties["version"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_INT32).UnPack(var_Version)

		if err != nil {
			panic(err)
		}

		var_Version_mapped := val.(int32)

		s.Version = var_Version_mapped
	}
	if properties["auditData"] != nil && properties["auditData"].AsInterface() != nil {

		var_AuditData := properties["auditData"]
		var mappedValue = CodeAuditDataMapperInstance.FromProperties(var_AuditData.GetStructValue().Fields)

		var_AuditData_mapped := mappedValue

		s.AuditData = var_AuditData_mapped
	}
	return s
}

func (m *CodeMapper) ToUnstructured(code *Code) unstructured.Unstructured {
	var properties unstructured.Unstructured = make(unstructured.Unstructured)
	properties["type"] = "nano/Code"

	var_Id := code.Id

	if var_Id != nil {
		var var_Id_mapped interface{}

		var_Id_mapped = var_Id.String()
		properties["id"] = var_Id_mapped
	}

	var_ContentFormat := code.ContentFormat

	var var_ContentFormat_mapped interface{}

	var_ContentFormat_mapped = string(var_ContentFormat)
	properties["contentFormat"] = var_ContentFormat_mapped

	var_Annotations := code.Annotations

	if var_Annotations != nil {
		var var_Annotations_mapped interface{}

		var var_Annotations_st map[string]interface{} = make(map[string]interface{})
		for key, value := range var_Annotations {

			var_1x := value
			var var_1x_mapped interface{}

			var_1x_mapped = var_1x

			var_Annotations_st[key] = var_1x_mapped
		}
		var_Annotations_mapped = var_Annotations_st
		properties["annotations"] = var_Annotations_mapped
	}

	var_Name := code.Name

	var var_Name_mapped interface{}

	var_Name_mapped = var_Name
	properties["name"] = var_Name_mapped

	var_Language := code.Language

	var var_Language_mapped interface{}

	var_Language_mapped = string(var_Language)
	properties["language"] = var_Language_mapped

	var_Content := code.Content

	var var_Content_mapped interface{}

	var_Content_mapped = var_Content
	properties["content"] = var_Content_mapped

	var_Version := code.Version

	var var_Version_mapped interface{}

	var_Version_mapped = var_Version
	properties["version"] = var_Version_mapped

	var_AuditData := code.AuditData

	if var_AuditData != nil {
		var var_AuditData_mapped interface{}

		var_AuditData_mapped = CodeAuditDataMapperInstance.ToUnstructured(var_AuditData)
		properties["auditData"] = var_AuditData_mapped
	}

	return properties
}

type CodeAuditDataMapper struct {
}

func NewCodeAuditDataMapper() *CodeAuditDataMapper {
	return &CodeAuditDataMapper{}
}

var CodeAuditDataMapperInstance = NewCodeAuditDataMapper()

func (m *CodeAuditDataMapper) New() *CodeAuditData {
	return &CodeAuditData{}
}

func (m *CodeAuditDataMapper) ResourceIdentity() abs.ResourceIdentity {
	return abs.ResourceIdentity{
		Namespace: "nano",
		Name:      "Code",
	}
}

func (m *CodeAuditDataMapper) ToProperties(codeAuditData *CodeAuditData) map[string]*structpb.Value {
	var properties = make(map[string]*structpb.Value)

	var_CreatedBy := codeAuditData.CreatedBy

	if var_CreatedBy != nil {
		var var_CreatedBy_mapped *structpb.Value

		var var_CreatedBy_err error
		var_CreatedBy_mapped, var_CreatedBy_err = types.ByResourcePropertyType(model.ResourceProperty_STRING).Pack(*var_CreatedBy)
		if var_CreatedBy_err != nil {
			panic(var_CreatedBy_err)
		}
		properties["createdBy"] = var_CreatedBy_mapped
	}

	var_UpdatedBy := codeAuditData.UpdatedBy

	if var_UpdatedBy != nil {
		var var_UpdatedBy_mapped *structpb.Value

		var var_UpdatedBy_err error
		var_UpdatedBy_mapped, var_UpdatedBy_err = types.ByResourcePropertyType(model.ResourceProperty_STRING).Pack(*var_UpdatedBy)
		if var_UpdatedBy_err != nil {
			panic(var_UpdatedBy_err)
		}
		properties["updatedBy"] = var_UpdatedBy_mapped
	}

	var_CreatedOn := codeAuditData.CreatedOn

	if var_CreatedOn != nil {
		var var_CreatedOn_mapped *structpb.Value

		var var_CreatedOn_err error
		var_CreatedOn_mapped, var_CreatedOn_err = types.ByResourcePropertyType(model.ResourceProperty_TIMESTAMP).Pack(*var_CreatedOn)
		if var_CreatedOn_err != nil {
			panic(var_CreatedOn_err)
		}
		properties["createdOn"] = var_CreatedOn_mapped
	}

	var_UpdatedOn := codeAuditData.UpdatedOn

	if var_UpdatedOn != nil {
		var var_UpdatedOn_mapped *structpb.Value

		var var_UpdatedOn_err error
		var_UpdatedOn_mapped, var_UpdatedOn_err = types.ByResourcePropertyType(model.ResourceProperty_TIMESTAMP).Pack(*var_UpdatedOn)
		if var_UpdatedOn_err != nil {
			panic(var_UpdatedOn_err)
		}
		properties["updatedOn"] = var_UpdatedOn_mapped
	}
	return properties
}

func (m *CodeAuditDataMapper) FromProperties(properties map[string]*structpb.Value) *CodeAuditData {
	var s = m.New()
	if properties["createdBy"] != nil && properties["createdBy"].AsInterface() != nil {

		var_CreatedBy := properties["createdBy"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_STRING).UnPack(var_CreatedBy)

		if err != nil {
			panic(err)
		}

		var_CreatedBy_mapped := new(string)
		*var_CreatedBy_mapped = val.(string)

		s.CreatedBy = var_CreatedBy_mapped
	}
	if properties["updatedBy"] != nil && properties["updatedBy"].AsInterface() != nil {

		var_UpdatedBy := properties["updatedBy"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_STRING).UnPack(var_UpdatedBy)

		if err != nil {
			panic(err)
		}

		var_UpdatedBy_mapped := new(string)
		*var_UpdatedBy_mapped = val.(string)

		s.UpdatedBy = var_UpdatedBy_mapped
	}
	if properties["createdOn"] != nil && properties["createdOn"].AsInterface() != nil {

		var_CreatedOn := properties["createdOn"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_TIMESTAMP).UnPack(var_CreatedOn)

		if err != nil {
			panic(err)
		}

		var_CreatedOn_mapped := new(time.Time)
		*var_CreatedOn_mapped = val.(time.Time)

		s.CreatedOn = var_CreatedOn_mapped
	}
	if properties["updatedOn"] != nil && properties["updatedOn"].AsInterface() != nil {

		var_UpdatedOn := properties["updatedOn"]
		val, err := types.ByResourcePropertyType(model.ResourceProperty_TIMESTAMP).UnPack(var_UpdatedOn)

		if err != nil {
			panic(err)
		}

		var_UpdatedOn_mapped := new(time.Time)
		*var_UpdatedOn_mapped = val.(time.Time)

		s.UpdatedOn = var_UpdatedOn_mapped
	}
	return s
}

func (m *CodeAuditDataMapper) ToUnstructured(codeAuditData *CodeAuditData) unstructured.Unstructured {
	var properties unstructured.Unstructured = make(unstructured.Unstructured)
	properties["type"] = "nano/Code"

	var_CreatedBy := codeAuditData.CreatedBy

	if var_CreatedBy != nil {
		var var_CreatedBy_mapped interface{}

		var_CreatedBy_mapped = *var_CreatedBy
		properties["createdBy"] = var_CreatedBy_mapped
	}

	var_UpdatedBy := codeAuditData.UpdatedBy

	if var_UpdatedBy != nil {
		var var_UpdatedBy_mapped interface{}

		var_UpdatedBy_mapped = *var_UpdatedBy
		properties["updatedBy"] = var_UpdatedBy_mapped
	}

	var_CreatedOn := codeAuditData.CreatedOn

	if var_CreatedOn != nil {
		var var_CreatedOn_mapped interface{}

		var_CreatedOn_mapped = *var_CreatedOn
		properties["createdOn"] = var_CreatedOn_mapped
	}

	var_UpdatedOn := codeAuditData.UpdatedOn

	if var_UpdatedOn != nil {
		var var_UpdatedOn_mapped interface{}

		var_UpdatedOn_mapped = *var_UpdatedOn
		properties["updatedOn"] = var_UpdatedOn_mapped
	}

	return properties
}