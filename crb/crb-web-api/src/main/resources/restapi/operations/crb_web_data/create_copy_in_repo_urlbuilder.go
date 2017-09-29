// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package crb_web_data

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the generate command

import (
	"errors"
	"net/url"
	golangswaggerpaths "path"
	"strings"
)

// CreateCopyInRepoURL generates an URL for the create copy in repo operation
type CreateCopyInRepoURL struct {
	CopyID string
	RepoID string

	_basePath string
	// avoid unkeyed usage
	_ struct{}
}

// WithBasePath sets the base path for this url builder, only required when it's different from the
// base path specified in the swagger spec.
// When the value of the base path is an empty string
func (o *CreateCopyInRepoURL) WithBasePath(bp string) *CreateCopyInRepoURL {
	o.SetBasePath(bp)
	return o
}

// SetBasePath sets the base path for this url builder, only required when it's different from the
// base path specified in the swagger spec.
// When the value of the base path is an empty string
func (o *CreateCopyInRepoURL) SetBasePath(bp string) {
	o._basePath = bp
}

// Build a url path and query string
func (o *CreateCopyInRepoURL) Build() (*url.URL, error) {
	var result url.URL

	var _path = "/repositories/{repoId}/copies/{copyId}/data"

	copyID := o.CopyID
	if copyID != "" {
		_path = strings.Replace(_path, "{copyId}", copyID, -1)
	} else {
		return nil, errors.New("CopyID is required on CreateCopyInRepoURL")
	}
	repoID := o.RepoID
	if repoID != "" {
		_path = strings.Replace(_path, "{repoId}", repoID, -1)
	} else {
		return nil, errors.New("RepoID is required on CreateCopyInRepoURL")
	}
	_basePath := o._basePath
	if _basePath == "" {
		_basePath = "/crb"
	}
	result.Path = golangswaggerpaths.Join(_basePath, _path)

	return &result, nil
}

// Must is a helper function to panic when the url builder returns an error
func (o *CreateCopyInRepoURL) Must(u *url.URL, err error) *url.URL {
	if err != nil {
		panic(err)
	}
	if u == nil {
		panic("url can't be nil")
	}
	return u
}

// String returns the string representation of the path with query string
func (o *CreateCopyInRepoURL) String() string {
	return o.Must(o.Build()).String()
}

// BuildFull builds a full url with scheme, host, path and query string
func (o *CreateCopyInRepoURL) BuildFull(scheme, host string) (*url.URL, error) {
	if scheme == "" {
		return nil, errors.New("scheme is required for a full url on CreateCopyInRepoURL")
	}
	if host == "" {
		return nil, errors.New("host is required for a full url on CreateCopyInRepoURL")
	}

	base, err := o.Build()
	if err != nil {
		return nil, err
	}

	base.Scheme = scheme
	base.Host = host
	return base, nil
}

// StringFull returns the string representation of a complete url
func (o *CreateCopyInRepoURL) StringFull(scheme, host string) string {
	return o.Must(o.BuildFull(scheme, host)).String()
}