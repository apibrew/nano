// Code generated by apbr generate. DO NOT EDIT.
// versions:
// 	apbr generate v1.2

//go:build !codeanalysis

package model

import "github.com/google/uuid"
import "time"

type Job struct {
	Id                 *uuid.UUID        `json:"id,omitempty"`
	NextExecutionTime  time.Time         `json:"nextExecutionTime,omitempty"`
	Language           JobLanguage       `json:"language,omitempty"`
	Source             string            `json:"source,omitempty"`
	ContentFormat      JobContentFormat  `json:"contentFormat,omitempty"`
	Annotations        map[string]string `json:"annotations,omitempty"`
	LastExecutionTime  *time.Time        `json:"lastExecutionTime,omitempty"`
	LastExecutionError *string           `json:"lastExecutionError,omitempty"`
	Name               string            `json:"name,omitempty"`
	Version            int32             `json:"version,omitempty"`
	AuditData          *JobAuditData     `json:"auditData,omitempty"`
}

func (s Job) GetId() *uuid.UUID {
	return s.Id
}
func (s Job) GetNextExecutionTime() time.Time {
	return s.NextExecutionTime
}
func (s Job) GetLanguage() JobLanguage {
	return s.Language
}
func (s Job) GetSource() string {
	return s.Source
}
func (s Job) GetContentFormat() JobContentFormat {
	return s.ContentFormat
}
func (s Job) GetAnnotations() map[string]string {
	return s.Annotations
}
func (s Job) GetLastExecutionTime() *time.Time {
	return s.LastExecutionTime
}
func (s Job) GetLastExecutionError() *string {
	return s.LastExecutionError
}
func (s Job) GetName() string {
	return s.Name
}
func (s Job) GetVersion() int32 {
	return s.Version
}
func (s Job) GetAuditData() *JobAuditData {
	return s.AuditData
}

type JobAuditData struct {
	CreatedBy *string    `json:"createdBy,omitempty"`
	UpdatedBy *string    `json:"updatedBy,omitempty"`
	CreatedOn *time.Time `json:"createdOn,omitempty"`
	UpdatedOn *time.Time `json:"updatedOn,omitempty"`
}

func (s JobAuditData) GetCreatedBy() *string {
	return s.CreatedBy
}
func (s JobAuditData) GetUpdatedBy() *string {
	return s.UpdatedBy
}
func (s JobAuditData) GetCreatedOn() *time.Time {
	return s.CreatedOn
}
func (s JobAuditData) GetUpdatedOn() *time.Time {
	return s.UpdatedOn
}

type JobLanguage string

const (
	JobLanguage_JAVASCRIPT JobLanguage = "JAVASCRIPT"
	JobLanguage_TYPESCRIPT JobLanguage = "TYPESCRIPT"
)

type JobContentFormat string

const (
	JobContentFormat_TEXT  JobContentFormat = "TEXT"
	JobContentFormat_TAR   JobContentFormat = "TAR"
	JobContentFormat_TARGZ JobContentFormat = "TAR_GZ"
)
