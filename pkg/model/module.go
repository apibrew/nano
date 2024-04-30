// Code generated by apbr generate. DO NOT EDIT.
// versions:
// 	apbr generate v1.2

//go:build !codeanalysis

package model

import "github.com/google/uuid"
import "time"

type Module struct {
	Id            *uuid.UUID          `json:"id,omitempty"`
	Annotations   map[string]string   `json:"annotations,omitempty"`
	Name          string              `json:"name,omitempty"`
	Language      ModuleLanguage      `json:"language,omitempty"`
	Source        string              `json:"source,omitempty"`
	ContentFormat ModuleContentFormat `json:"contentFormat,omitempty"`
	Version       int32               `json:"version,omitempty"`
	AuditData     *ModuleAuditData    `json:"auditData,omitempty"`
}

func (s Module) GetId() *uuid.UUID {
	return s.Id
}
func (s Module) GetAnnotations() map[string]string {
	return s.Annotations
}
func (s Module) GetName() string {
	return s.Name
}
func (s Module) GetLanguage() ModuleLanguage {
	return s.Language
}
func (s Module) GetSource() string {
	return s.Source
}
func (s Module) GetContentFormat() ModuleContentFormat {
	return s.ContentFormat
}
func (s Module) GetVersion() int32 {
	return s.Version
}
func (s Module) GetAuditData() *ModuleAuditData {
	return s.AuditData
}

type ModuleAuditData struct {
	CreatedBy *string    `json:"createdBy,omitempty"`
	UpdatedBy *string    `json:"updatedBy,omitempty"`
	CreatedOn *time.Time `json:"createdOn,omitempty"`
	UpdatedOn *time.Time `json:"updatedOn,omitempty"`
}

func (s ModuleAuditData) GetCreatedBy() *string {
	return s.CreatedBy
}
func (s ModuleAuditData) GetUpdatedBy() *string {
	return s.UpdatedBy
}
func (s ModuleAuditData) GetCreatedOn() *time.Time {
	return s.CreatedOn
}
func (s ModuleAuditData) GetUpdatedOn() *time.Time {
	return s.UpdatedOn
}

type ModuleLanguage string

const (
	ModuleLanguage_JAVASCRIPT ModuleLanguage = "JAVASCRIPT"
	ModuleLanguage_TYPESCRIPT ModuleLanguage = "TYPESCRIPT"
)

type ModuleContentFormat string

const (
	ModuleContentFormat_TEXT  ModuleContentFormat = "TEXT"
	ModuleContentFormat_TAR   ModuleContentFormat = "TAR"
	ModuleContentFormat_TARGZ ModuleContentFormat = "TAR_GZ"
)