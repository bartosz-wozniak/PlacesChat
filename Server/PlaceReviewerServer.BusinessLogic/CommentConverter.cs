using PlaceReviewerServer.DataAccess;

namespace PlaceReviewerServer.BusinessLogic
{
    public static class CommentConverter
    {
        public static DtoComment DataAccsessToDto(Comments d)
        {
            return new DtoComment
            {
                Id = d.id,
                Comment = d.comment,
                Date = d.date,
                Login = d.Users.login,
                PlaceId = d.placeId
            };
        }

        public static Comments DtoToDataAccess(DtoComment c)
        {
            return new Comments
            {
                id = c.Id,
                date = c.Date,
                userId = new UserLogic().GetUser(c.Login).Id,
                placeId = c.PlaceId,
                comment = c.Comment
            };
        }
    }
}