namespace PlaceReviewerServer.BusinessLogic
{
    public class DtoComment
    {
        public int Id { get; set; }

        public string PlaceId { get; set; }

        public string Comment { get; set; }

        public string Date { get; set; }

        public string Login { get; set; }
    }
}