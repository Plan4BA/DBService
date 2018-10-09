package de.ddkfm.plan4ba.controller

import de.ddkfm.plan4ba.models.*
import de.ddkfm.plan4ba.utils.*
import io.swagger.annotations.*
import spark.Request
import spark.Response
import javax.ws.rs.*

@Api(value = "/lectures", description = "all operations about lectures")
@Path("/lectures")
@Produces("application/json")
class LectureController(req : Request, resp : Response) : ControllerInterface(req = req, resp = resp) {

    @GET
    @ApiOperation(value = "list all lectures", notes = "return all lectures")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Lecture::class, responseContainer = "List")
    )
    @Path("")
    fun allLectures() : Any? = HibernateUtils.doInHibernate { session ->
        val lectures = session.createQuery("From HibernateLecture", HibernateLecture::class.java)
                .list()
                .map { it.toLecture() }
        lectures
    }

    @GET
    @ApiOperation(value = "get a specific lecture", notes = "get a specific lecture")
    @ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    @ApiResponses(
            ApiResponse(code = 200, message = "successfull", response = Lecture::class),
            ApiResponse(code = 404, response = NotFound::class, message = "Not Found")
    )
    @Path("/:id")
    fun getLecture(@ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var lecture = session.find(HibernateLecture::class.java, id)
            lecture?.toLecture() ?: NotFound()
        }
    }

    @PUT
    @ApiOperation(value = "create a lecture")
    @Path("")
    @ApiResponses(
            ApiResponse(code = 201, message = "lecture created", response = Lecture::class),
            ApiResponse(code = 409, message = "lecture already exists", response = AlreadyExists::class),
            ApiResponse(code = 500, message = "Could not save the lecture", response = HttpStatus::class)
    )
    fun createLecture(@ApiParam lecture : Lecture) : Any? {
        return HibernateUtils.doInHibernate { session ->
            var groupSQL = ""
            var userSQL = ""
            if(lecture.groupId != null && lecture.groupId!! > 0) {
                groupSQL = "AND group_id = ${lecture.groupId}"
            }
            if(lecture.userId != null && lecture.userId!! > 0) {
                val user = session.find(HibernateUser::class.java, lecture.userId)
                if(user != null) {
                    userSQL = "AND user_id = $user.id"
                    groupSQL = "AND group_id = ${user.group.id}"
                }
            }

            var existingLecture = session.createQuery("From HibernateLecture Where title = '${lecture.title}'" +
                    " AND start = ${lecture.start} AND end = ${lecture.end}" +
                    " $userSQL $groupSQL", HibernateLecture::class.java).uniqueResultOptional()


            if(!existingLecture.isPresent)
                AlreadyExists("Lecture already exists")
            else {
                session.beginTransaction()
                try {

                    session.persist(lecture.toHibernateLecture())
                    session.transaction.commit()
                    lecture
                } catch (e : Exception) {
                    e.printStackTrace()
                    session.transaction.rollback()
                    HttpStatus(500, "Could not save the lecture")
                }
            }
        }
    }

    @POST
    @ApiOperation(value = "updates an Lecture")
    @Path("/:id")
    @ApiImplicitParams(
            ApiImplicitParam(name = "id", paramType = "path", dataType = "integer")
    )
    @ApiResponses(
            ApiResponse(code = 200, message = "lecture updated", response = Lecture::class),
            ApiResponse(code = 500, message = "Could not edit the Lecture", response = HttpStatus::class)
    )
    fun updateLecture(@ApiParam lecture : Lecture,
                      @ApiParam(hidden = true) id : Int) : Any? {
        return HibernateUtils.doInHibernate { session ->
            val existingLecture = session.find(HibernateLecture::class.java, id)
            if(existingLecture == null)
                NotFound("Lecture does not exist")

            existingLecture.allDay = lecture.allDay
            existingLecture.color = lecture.color
            existingLecture.description = lecture.description
            existingLecture.start = lecture.start
            existingLecture.end = lecture.end
            existingLecture.exam = lecture.exam
            existingLecture.instructor = lecture.instructor
            existingLecture.remarks = lecture.remarks
            existingLecture.room = lecture.room
            existingLecture.sroom = lecture.sroom
            existingLecture.title = lecture.title
            if(lecture.groupId != null || lecture.groupId!! > 0) {
                //Update Lecture to group-specific
                val group = session.find(HibernateUserGroup::class.java, lecture.groupId)
                existingLecture.group = group
            }
            if(lecture.userId != null || lecture.userId!! > 0) {
                val user = session.find(HibernateUser::class.java, lecture.userId)
                existingLecture.user = user
            }
            try {
                session.persist(existingLecture)
                existingLecture.toLecture()
            } catch (e : Exception) {
                e.printStackTrace()
                BadRequest("Could not update the lecture")
            }

        }
    }
}