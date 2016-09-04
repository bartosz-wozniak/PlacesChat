using System;
using PlaceReviewerServer.DataAccess;

namespace PlaceReviewerServer.BusinessLogic
{
    public static class UserConverter
    {
        public static DtoUser DataAccsessToDto(Users d)
        {
            var dto = new DtoUser
            {
                Id = d.id,
                Email = d.email,          
                Login = d.login,
                Password = d.password
            };
            if (d.image != null)
                dto.Image = Convert.ToBase64String(d.image);
            return dto;
        }

        public static Users DtoToDataAccess(DtoUser c)
        {
            var user = new Users
            {
                id = c.Id,
                password = c.Password,
                email = c.Email,
                login = c.Login          
            };
            if (c.Image != null)
                user.image = Convert.FromBase64String(c.Image);
            return user;
        }
    }
}