// Copyright (c) [2017] Dell Inc. or its subsidiaries. All Rights Reserved.
package crb_web

// This file was generated by the swagger tool.
// Editing this file might prove futile when you re-run the generate command

import (
	"net/http"

	middleware "github.com/go-openapi/runtime/middleware"
)

// GetCopyInstancesHandlerFunc turns a function with the right signature into a get copy instances handler
type GetCopyInstancesHandlerFunc func(GetCopyInstancesParams) middleware.Responder

// Handle executing the request and returning a response
func (fn GetCopyInstancesHandlerFunc) Handle(params GetCopyInstancesParams) middleware.Responder {
	return fn(params)
}

// GetCopyInstancesHandler interface for that can handle valid get copy instances params
type GetCopyInstancesHandler interface {
	Handle(GetCopyInstancesParams) middleware.Responder
}

// NewGetCopyInstances creates a new http.Handler for the get copy instances operation
func NewGetCopyInstances(ctx *middleware.Context, handler GetCopyInstancesHandler) *GetCopyInstances {
	return &GetCopyInstances{Context: ctx, Handler: handler}
}

/*GetCopyInstances swagger:route GET /copies crb-web getCopyInstances

List all the current copy instances.

Return copyIds from Metadata-DB

*/
type GetCopyInstances struct {
	Context *middleware.Context
	Handler GetCopyInstancesHandler
}

func (o *GetCopyInstances) ServeHTTP(rw http.ResponseWriter, r *http.Request) {
	route, _ := o.Context.RouteInfo(r)
	var Params = NewGetCopyInstancesParams()

	if err := o.Context.BindValidRequest(r, route, &Params); err != nil { // bind params
		o.Context.Respond(rw, r, route.Produces, route, err)
		return
	}

	res := o.Handler.Handle(Params) // actually handle the request

	o.Context.Respond(rw, r, route.Produces, route, res)

}
