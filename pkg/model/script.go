// Code generated by apbr generate. DO NOT EDIT.
// versions:
// 	apbr generate v1.2

//go:build !codeanalysis

package model

import "github.com/google/uuid"
import "time"

type Script struct {
	Id            *uuid.UUID          `json:"id,omitempty"`
	ContentFormat ScriptContentFormat `json:"contentFormat,omitempty"`
	Annotations   map[string]string   `json:"annotations,omitempty"`
	Output        interface{}         `json:"output,omitempty"`
	Language      ScriptLanguage      `json:"language,omitempty"`
	Source        string              `json:"source,omitempty"`
	Version       int32               `json:"version,omitempty"`
	AuditData     *ScriptAuditData    `json:"auditData,omitempty"`
}

func (s Script) GetId() *uuid.UUID {
	return s.Id
}
func (s Script) GetContentFormat() ScriptContentFormat {
	return s.ContentFormat
}
func (s Script) GetAnnotations() map[string]string {
	return s.Annotations
}
func (s Script) GetOutput() interface{} {
	return s.Output
}
func (s Script) GetLanguage() ScriptLanguage {
	return s.Language
}
func (s Script) GetSource() string {
	return s.Source
}
func (s Script) GetVersion() int32 {
	return s.Version
}
func (s Script) GetAuditData() *ScriptAuditData {
	return s.AuditData
}

type ScriptAuditData struct {
	CreatedBy *string    `json:"createdBy,omitempty"`
	UpdatedBy *string    `json:"updatedBy,omitempty"`
	CreatedOn *time.Time `json:"createdOn,omitempty"`
	UpdatedOn *time.Time `json:"updatedOn,omitempty"`
}

func (s ScriptAuditData) GetCreatedBy() *string {
	return s.CreatedBy
}
func (s ScriptAuditData) GetUpdatedBy() *string {
	return s.UpdatedBy
}
func (s ScriptAuditData) GetCreatedOn() *time.Time {
	return s.CreatedOn
}
func (s ScriptAuditData) GetUpdatedOn() *time.Time {
	return s.UpdatedOn
}

type ScriptContentFormat string

const (
	ScriptContentFormat_TEXT  ScriptContentFormat = "TEXT"
	ScriptContentFormat_TAR   ScriptContentFormat = "TAR"
	ScriptContentFormat_TARGZ ScriptContentFormat = "TAR_GZ"
)

type ScriptLanguage string

const (
	ScriptLanguage_JAVASCRIPT ScriptLanguage = "JAVASCRIPT"
)