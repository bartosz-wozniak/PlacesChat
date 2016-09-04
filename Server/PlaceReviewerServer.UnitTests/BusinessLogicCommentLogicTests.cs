using System.Collections.Generic;
using System.Data.Entity;
using System.Data.Entity.Infrastructure;
using System.Linq;
using System.Threading.Tasks;
using PlaceReviewerServer.BusinessLogic;
using PlaceReviewerServer.DataAccess;
using Microsoft.VisualStudio.TestTools.UnitTesting;
using Moq;

namespace PlaceReviewerServer.UnitTests
{
    [TestClass]
    public class BusinessLogicCommentLogicTests
    {
        [TestMethod]
        public async Task SaveComment_AddingValidComment_CommentAdded()
        {
            var data = new List<Comments>().AsQueryable();
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Comments).Returns(mockSet.Object);
            var commentLogic = new CommentLogic { Context = mockContext.Object };
            var b = await commentLogic.SaveComment(new DtoComment());
            Assert.AreEqual(b, 1);
            mockSet.Verify(m => m.Add(It.IsAny<Comments>()), Times.Once());
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Once());
        }

        [TestMethod]
        public async Task SaveComment_UpdatingValidComment_CommentUpdated()
        {
            var data = new List<Comments>
            {
                new Comments
                {
                    id = 999,
                    comment = "q",
                    placeId = "a",
                    userId = 1
                }
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Comments).Returns(mockSet.Object);
            var commentLogic = new CommentLogic { Context = mockContext.Object };
            var b = await commentLogic.SaveComment(new DtoComment
            { Id = 999, Login = "qqqqqqqqqqq", Comment = "aaaa", PlaceId = "b@b.b" });
            Assert.AreEqual(b, 1);
            mockSet.Verify(m => m.Add(It.IsAny<Comments>()), Times.Never);
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Once());
        }

        [TestMethod]
        public async Task GetComment_ReturningCommentWithSprecifiedId_OneSpecifiedCommentReturned()
        {
            var data = new List<Comments>
            {
                new Comments {id = 1, comment = "BBB", userId = 1, Users = new Users { id = 1} },
                new Comments {id = 2, comment = "ZZZ"},
                new Comments {id = 3, comment = "AAA"}
            }.AsQueryable();
            var userData = new List<Users>
            {
                new Users
                {
                    id = 1
                }
            }.AsQueryable();
            var userMockSet = new Mock<DbSet<Users>>();
            userMockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(userData.GetEnumerator()));
            userMockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(userData.Provider));
            userMockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(userData.Expression);
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(c => c.Comments).Returns(mockSet.Object);
            mockContext.Setup(c => c.Users).Returns(userMockSet.Object);
            var commentLogic = new CommentLogic { Context = mockContext.Object };
            var comment = await commentLogic.GetComment(1);
            Assert.AreEqual(1, comment.Id);
            Assert.AreEqual("BBB", comment.Comment);
        }

        [TestMethod]
        public async Task GetComment_ReturningCommentWithSprecifiedId_NullReturned()
        {
            var data = new List<Comments>
            {
                new Comments {id = 1, comment = "BBB"},
                new Comments {id = 2, comment = "ZZZ"},
                new Comments {id = 3, comment = "AAA"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(c => c.Comments).Returns(mockSet.Object);
            var logic = new CommentLogic { Context = mockContext.Object };
            var comment = await logic.GetComment(4);
            Assert.AreEqual(comment.Id, -1);
        }

        [TestMethod]
        public async Task RemoveComment_RemovingComment_CommentRemoved()
        {
            var data = new List<Comments>
            {
                new Comments {id = 1, comment = "BBB"},
                new Comments {id = 2, comment = "ZZZ"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Comments).Returns(mockSet.Object);
            var commentLogic = new CommentLogic { Context = mockContext.Object };
            var b = await commentLogic.RemoveComment(1);
            Assert.AreEqual(b, 1);
            mockSet.Verify(m => m.Remove(It.IsAny<Comments>()), Times.Once());
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Once());
        }

        [TestMethod]
        public async Task RemoveComment_RemovingComment_MethodFailed()
        {
            var data = new List<Comments>
            {
                new Comments {id = 1, comment = "BBB"},
                new Comments {id = 2, comment = "ZZZ"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Comments).Returns(mockSet.Object);
            var commentLogic = new CommentLogic { Context = mockContext.Object };
            var b = await commentLogic.RemoveComment(5);
            Assert.AreEqual(b, 0);
            mockSet.Verify(m => m.Remove(It.IsAny<Comments>()), Times.Never);
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Never);
        }

        [TestMethod]
        public async Task GetAllComments_ReturningAllComments_AllCommentsReturned()
        {
            var data = new List<Comments>
            {
                new Comments {comment = "BBB", userId = 1, Users = new Users { id = 1}, placeId = "a"},
                new Comments {comment = "ZZZ", userId = 1, Users = new Users { id = 1}, placeId = "a"},
                new Comments {comment = "AAA", userId = 1, Users = new Users { id = 1}, placeId = "a"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Comments>>();
            mockSet.As<IDbAsyncEnumerable<Comments>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Comments>(data.GetEnumerator()));
            mockSet.As<IQueryable<Comments>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Comments>(data.Provider));
            mockSet.As<IQueryable<Comments>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Comments>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(c => c.Comments).Returns(mockSet.Object);
            var logic = new CommentLogic { Context = mockContext.Object };
            var com = await logic.GetComments("a");
            Assert.AreEqual(3, com.Length);
            Assert.AreEqual("BBB", com[0].Comment);
            Assert.AreEqual("ZZZ", com[1].Comment);
            Assert.AreEqual("AAA", com[2].Comment);
        }
    }
}