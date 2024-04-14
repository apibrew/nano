// Code generated by apbr generate. DO NOT EDIT.
// versions:
// 	apbr generate v1.2

//go:build !codeanalysis

package model

import "github.com/google/uuid"
import "time"

type Code struct {
	Id            *uuid.UUID        `json:"id,omitempty"`
	Annotations   map[string]string `json:"annotations,omitempty"`
	Name          string            `json:"name,omitempty"`
	Language      CodeLanguage      `json:"language,omitempty"`
	Content       string            `json:"content,omitempty"`
	ContentFormat CodeContentFormat `json:"contentFormat,omitempty"`
	Version       int32             `json:"version,omitempty"`
	AuditData     *CodeAuditData    `json:"auditData,omitempty"`
}

func (s Code) GetId() *uuid.UUID {
	return s.Id
}
func (s Code) GetAnnotations() map[string]string {
	return s.Annotations
}
func (s Code) GetName() string {
	return s.Name
}
func (s Code) GetLanguage() CodeLanguage {
	return s.Language
}
func (s Code) GetContent() string {
	return s.Content
}
func (s Code) GetContentFormat() CodeContentFormat {
	return s.ContentFormat
}
func (s Code) GetVersion() int32 {
	return s.Version
}
func (s Code) GetAuditData() *CodeAuditData {
	return s.AuditData
}

type CodeAuditData struct {
	CreatedBy *string    `json:"createdBy,omitempty"`
	UpdatedBy *string    `json:"updatedBy,omitempty"`
	CreatedOn *time.Time `json:"createdOn,omitempty"`
	UpdatedOn *time.Time `json:"updatedOn,omitempty"`
}

func (s CodeAuditData) GetCreatedBy() *string {
	return s.CreatedBy
}
func (s CodeAuditData) GetUpdatedBy() *string {
	return s.UpdatedBy
}
func (s CodeAuditData) GetCreatedOn() *time.Time {
	return s.CreatedOn
}
func (s CodeAuditData) GetUpdatedOn() *time.Time {
	return s.UpdatedOn
}

type CodeLanguage string

const (
	CodeLanguage_JAVASCRIPT CodeLanguage = "JAVASCRIPT"
)

type CodeContentFormat string

const (
	CodeContentFormat_TEXT  CodeContentFormat = "TEXT"
	CodeContentFormat_TAR   CodeContentFormat = "TAR"
	CodeContentFormat_TARGZ CodeContentFormat = "TAR_GZ"
)
