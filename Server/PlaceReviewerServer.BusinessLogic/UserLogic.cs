using System;
using System.Data.Entity;
using System.Linq;
using System.Threading.Tasks;
using PlaceReviewerServer.DataAccess;

namespace PlaceReviewerServer.BusinessLogic
{
    public class UserLogic
    {
        public UserLogic()
        {
            Context = new PlaceReviewerDbEntities();
        }

        public PlaceReviewerDbEntities Context { get; set; }

        public DtoUser GetUser(int userId)
        {
            try
            {
                using (var data = Context)
                    return
                        UserConverter.DataAccsessToDto(
                                (from item in data.Users where item.id == userId select item)
                                    .FirstOrDefault());
            }
            catch (Exception)
            {
                return new DtoUser { Id = -1 };
            }
        }

        public DtoUser GetUser(string login)
        {
            try
            {
                using (var data = Context)
                    return
                        UserConverter.DataAccsessToDto(
                                (from item in data.Users where item.login == login select item)
                                    .FirstOrDefault());
            }
            catch (Exception)
            {
                return new DtoUser { Id = -1 };
            }
        }

        public DtoUser GetUser(string login, string password)
        {
            try
            {
                using (var data = Context)
                    return
                        UserConverter.DataAccsessToDto(
                                (from item in data.Users where item.login == login && item.password == password select item)
                                    .FirstOrDefault());
            }
            catch (Exception)
            {
                return new DtoUser { Id = -1 };
            }
        }

        public async Task<int> SaveUser(DtoUser user)
        {
            try
            {
                using (var data = Context)
                {
                    var p =
                        await
                            (from item in data.Users where user.Id == item.id select item).FirstOrDefaultAsync();
                    // Updating
                    if (p != null)
                    {
                        p.login = user.Login;
                        p.email = user.Email;
                        if (user.Image != null)
                            p.image = Convert.FromBase64String(user.Image);
                        p.password = user.Password;
                    }
                    // Adding new
                    else
                    {
                        data.Users.Add(UserConverter.DtoToDataAccess(user));
                    }
                    await data.SaveChangesAsync();
                }
                return 1;
            }
            catch (Exception)
            {
                return 0;
            }
        }

        public async Task<int> RemoveUser(int userId)
        {
            try
            {
                using (var data = Context)
                {
                    var p = await (from item in data.Users where item.id == userId select item).FirstAsync();
                    data.Users.Remove(p);
                    await data.SaveChangesAsync();
                }
                return 1;
            }
            catch (Exception)
            {
                return 0;
            }
        }
    }
}
