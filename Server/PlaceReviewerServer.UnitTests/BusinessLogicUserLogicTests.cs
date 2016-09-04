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
    public class BusinessLogicUserLogicTests
    {
        [TestMethod]
        public async Task SaveUser_AddingValidUser_UserAdded()
        {
            var data = new List<Users>().AsQueryable();
            var mockSet = new Mock<DbSet<Users>>();
            mockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(data.GetEnumerator()));
            mockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(data.Provider));
            mockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(data.Expression);
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Users).Returns(mockSet.Object);
            var userLogic = new UserLogic { Context = mockContext.Object };
            var b = await userLogic.SaveUser(new DtoUser());
            Assert.AreEqual(b, 1);
            mockSet.Verify(m => m.Add(It.IsAny<Users>()), Times.Once());
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Once());
        }

        [TestMethod]
        public async Task SaveUser_UpdatingValidUser_UserUpdated()
        {
            var data = new List<Users>
            {
                new Users()
                {
                    id = 999,
                    login = "q",
                    password = "a",
                    email = "a@a.a"
                }
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Users>>();
            mockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(data.GetEnumerator()));
            mockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(data.Provider));
            mockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(data.Expression);
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Users).Returns(mockSet.Object);
            var userLogic = new UserLogic { Context = mockContext.Object };
            var b = await userLogic.SaveUser(new DtoUser
            { Id = 999, Login = "qqqqqqqqqqq", Password = "aaaa", Email = "b@b.b" });
            Assert.AreEqual(b, 1);
            mockSet.Verify(m => m.Add(It.IsAny<Users>()), Times.Never);
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Once());
        }

        [TestMethod]
        public void GetUser_ReturningUserWithSprecifiedId_OneSpecifiedUserReturned()
        {
            var data = new List<Users>
            {
                new Users {id = 1, login = "BBB"},
                new Users {id = 2, login = "ZZZ"},
                new Users {id = 3, login = "AAA"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Users>>();
            mockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(data.GetEnumerator()));
            mockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(data.Provider));
            mockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Users>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Users>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(c => c.Users).Returns(mockSet.Object);
            var userLogic = new UserLogic { Context = mockContext.Object };
            var user = userLogic.GetUser(1);
            Assert.AreEqual("BBB", user.Login);
        }

        [TestMethod]
        public void GetUser_ReturningUserWithSprecifiedId_NullReturned()
        {
            var data = new List<Users>
            {
                new Users {id = 1, login = "BBB"},
                new Users {id = 2, login = "ZZZ"},
                new Users {id = 3, login = "AAA"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Users>>();
            mockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(data.GetEnumerator()));
            mockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(data.Provider));
            mockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Users>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Users>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(c => c.Users).Returns(mockSet.Object);
            var logic = new UserLogic { Context = mockContext.Object };
            var user = logic.GetUser(4);
            Assert.AreEqual(user.Id, -1);
        }

        [TestMethod]
        public async Task RemoveUser_RemovingUser_UserRemoved()
        {
            var data = new List<Users>
            {
                new Users {id = 1, login = "BBB"},
                new Users {id = 2, login = "ZZZ"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Users>>();
            mockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(data.GetEnumerator()));
            mockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(data.Provider));
            mockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Users>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Users>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Users).Returns(mockSet.Object);
            var userLogic = new UserLogic { Context = mockContext.Object };
            var b = await userLogic.RemoveUser(1);
            Assert.AreEqual(b, 1);
            mockSet.Verify(m => m.Remove(It.IsAny<Users>()), Times.Once());
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Once());
        }

        [TestMethod]
        public async Task RemoveUser_RemovingUser_MethodFailed()
        {
            var data = new List<Users>
            {
                new Users {id = 1, login = "BBB"},
                new Users {id = 2, login = "ZZZ"}
            }.AsQueryable();
            var mockSet = new Mock<DbSet<Users>>();
            mockSet.As<IDbAsyncEnumerable<Users>>()
                .Setup(m => m.GetAsyncEnumerator())
                .Returns(new TestDbAsyncEnumerator<Users>(data.GetEnumerator()));
            mockSet.As<IQueryable<Users>>()
                .Setup(m => m.Provider)
                .Returns(new TestDbAsyncQueryProvider<Users>(data.Provider));
            mockSet.As<IQueryable<Users>>().Setup(m => m.Expression).Returns(data.Expression);
            mockSet.As<IQueryable<Users>>().Setup(m => m.ElementType).Returns(data.ElementType);
            mockSet.As<IQueryable<Users>>().Setup(m => m.GetEnumerator()).Returns(data.GetEnumerator());
            var mockContext = new Mock<PlaceReviewerDbEntities>();
            mockContext.Setup(m => m.Users).Returns(mockSet.Object);
            var userLogic = new UserLogic { Context = mockContext.Object };
            var b = await userLogic.RemoveUser(5);
            Assert.AreEqual(b, 0);
            mockSet.Verify(m => m.Remove(It.IsAny<Users>()), Times.Never);
            mockContext.Verify(m => m.SaveChangesAsync(), Times.Never);
        }
    }
}