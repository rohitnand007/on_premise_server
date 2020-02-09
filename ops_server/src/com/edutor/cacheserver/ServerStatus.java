package com.edutor.cacheserver;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path("/serverstatus")
public class ServerStatus {

 @GET
 @Produces(MediaType.APPLICATION_JSON)
 public Response getReplyServerStatus() {
	 return Response.ok("active").header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).header(HttpHeaders.CONTENT_LENGTH, "active".length()).build();
 }
 
 @POST
 @Produces(MediaType.APPLICATION_JSON)
 public Response postReplyServerStatus() {
	 return Response.ok("active").header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).header(HttpHeaders.CONTENT_LENGTH, "active".length()).build();
	 }
 
 @PUT
 @Produces(MediaType.APPLICATION_JSON)
 public Response putReplyServerStatus() {
	 return Response.ok("active").header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).header(HttpHeaders.CONTENT_LENGTH, "active".length()).build();
 }
 
 @DELETE
 @Produces(MediaType.APPLICATION_JSON)
 public Response deleteReplyServerStatus() {
	 return Response.ok("active").header(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN).header(HttpHeaders.CONTENT_LENGTH, "active".length()).build();
 }
 
} 