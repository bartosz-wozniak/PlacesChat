using System;
using System.Globalization;
using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Http;
using PlaceReviewerServer.BusinessLogic;

namespace PlaceReviewerServer.Web.Controllers
{
    public class CommentController : ApiController
    {
        [HttpGet]
        public async Task<HttpResponseMessage> GetComments()
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoComments { Comments = await new CommentLogic().GetAllComments() });
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> GetComment(int id)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, await new CommentLogic().GetComment(id));
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> GetComments(string placeId)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoComments { Comments = await new CommentLogic().GetComments(placeId) });
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> GetComments(string login, string password)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoComments { Comments = await new CommentLogic().GetComments(login, password) });
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> RemoveComment(int id)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoResponse { Success = await new CommentLogic().RemoveComment(id) });
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> SaveComment(string comment, int id, string login, string placeId)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoResponse
            {
                Success = await new CommentLogic().SaveComment(new DtoComment
                {
                    Comment = comment,
                    Id = id,
                    Login = login,
                    PlaceId = placeId,
                    Date = CommentLogic.GetDateTimeNow().ToString("g", CultureInfo.InvariantCulture)
                })                
            });
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> GetNewestComment(string placeId)
        {
            return Request.CreateResponse(HttpStatusCode.OK, await new CommentLogic().GetLastComment(placeId));
        }
    }
}
