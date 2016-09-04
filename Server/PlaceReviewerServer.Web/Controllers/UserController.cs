using System.Net;
using System.Net.Http;
using System.Threading.Tasks;
using System.Web.Http;
using PlaceReviewerServer.BusinessLogic;

namespace PlaceReviewerServer.Web.Controllers
{
    public class UserController : ApiController
    {
        [HttpGet]
        public HttpResponseMessage GetUser(string login, string password)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new UserLogic().GetUser(login, password));
            return response;
        }

        [HttpGet]
        public HttpResponseMessage GetUser(string login)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new UserLogic().GetUser(login));
            return response;
        }

        [HttpGet]
        public HttpResponseMessage GetUser(int id)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new UserLogic().GetUser(id));
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> RemoveUser(int id)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoResponse { Success = await new UserLogic().RemoveUser(id) });
            return response;
        }

        [HttpGet]
        public async Task<HttpResponseMessage> SaveUser(string login, int id, string email, string password, string image)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoResponse
            {
                Success = await new UserLogic().SaveUser(new DtoUser
                {
                    Password = password,
                    Id = id,
                    Login = login,
                    Image = image,
                    Email = email
                })
            });
            return response;
        }

        [HttpPost]
        public async Task<HttpResponseMessage> SaveUser(DtoUser user)
        {
            var response = Request.CreateResponse(HttpStatusCode.OK, new DtoResponse
            {
                Success = await new UserLogic().SaveUser(user)
            });
            return response;
        }
    }
}
