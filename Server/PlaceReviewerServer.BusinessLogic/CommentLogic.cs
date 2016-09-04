using System;
using System.Collections.ObjectModel;
using System.Data.Entity;
using System.Globalization;
using System.Linq;
using System.Threading.Tasks;
using PlaceReviewerServer.DataAccess;

namespace PlaceReviewerServer.BusinessLogic
{
    public class CommentLogic
    {
        public CommentLogic()
        {
            Context = new PlaceReviewerDbEntities();
        }

        public PlaceReviewerDbEntities Context { get; set; }

        public async Task<DtoComment[]> GetAllComments()
        {
            var ret = new ObservableCollection<DtoComment>();
            using (var data = Context)
                foreach (var item in await (from item in data.Comments select item).ToListAsync())
                    ret.Add(CommentConverter.DataAccsessToDto(item));
            return ret.ToArray();
        }

        public async Task<DtoComment> GetComment(int commentId)
        {
            try
            {
                using (var data = Context)
                {
                    var a = data.Comments;
                    var b = a.Where(item => item.userId == commentId);
                    var c = await b.FirstOrDefaultAsync();
                    return CommentConverter.DataAccsessToDto(c);
                }
            }
            catch (Exception ex)
            {
                return new DtoComment { Id = -1 };
            }
        }

        public async Task<DtoComment[]> GetComments(string placeId)
        {
            var ret = new ObservableCollection<DtoComment>();
            using (var data = Context)
                foreach (var item in await (from item in data.Comments where item.placeId == placeId select item).ToListAsync())
                    ret.Add(CommentConverter.DataAccsessToDto(item));
            return ret.ToArray();

        }

        public async Task<DtoComment[]> GetComments(string login, string password)
        {
            var ret = new ObservableCollection<DtoComment>();
            using (var data = Context)
                foreach (var item in await (from item in data.Comments where item.Users.login == login && item.Users.password == password select item).ToListAsync())
                    ret.Add(CommentConverter.DataAccsessToDto(item));
            return ret.ToArray();

        }

        public async Task<int> SaveComment(DtoComment comment)
        {
            try
            {
                using (var data = Context)
                {
                    var p =
                        await
                            (from item in data.Comments where comment.Id == item.id select item).FirstOrDefaultAsync();
                    // Updating
                    if (p != null)
                    {
                        p.comment = comment.Comment;
                        p.date = comment.Date;
                        p.userId = new UserLogic().GetUser(comment.Login).Id;
                        p.placeId = comment.PlaceId;
                    }
                    // Adding new
                    else
                    {
                        data.Comments.Add(CommentConverter.DtoToDataAccess(comment));
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

        public async Task<int> RemoveComment(int commentId)
        {
            try
            {
                using (var data = Context)
                {
                    var p = await (from item in data.Comments where item.id == commentId select item).FirstAsync();
                    data.Comments.Remove(p);
                    await data.SaveChangesAsync();
                }
                return 1;
            }
            catch (Exception)
            {
                return 0;
            }
        }

        public async Task<DtoComment> GetLastComment(string placeId)
        {
            using (var data = Context)
            {
                var places = data.Comments.Where(c => c.placeId == placeId);
                if (places.Any())
                {
                    int maxId = places.Max(com => com.id);
                    var comment = await data.Comments.SingleAsync(c => c.id == maxId);
                    DateTime date = DateTime.ParseExact(comment.date, "g", CultureInfo.InvariantCulture);
                    if (GetDateTimeNow().AddHours(-12) < date)
                        return CommentConverter.DataAccsessToDto(comment);
                    return null;
                }
                return null;
            }
        }

        public static DateTime GetDateTimeNow()
        {
            return TimeZoneInfo.ConvertTime(DateTime.Now,
                TimeZoneInfo.FindSystemTimeZoneById("Central European Standard Time"));
        }
    }
}
