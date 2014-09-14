var auctionModule = angular.module('auctionModule', []);

auctionModule.controller("AuctionCtrl", function ($scope, Auction, AuctionFacade, $location) {
    $scope.auction = Auction;

    $scope.findAll = function () {
        AuctionFacade.findAll()
                .success(function (data) {
                    $scope.auctions = data;
                }).error(function (data, status) {
            console.log("Auctions not found. Error: " + data + " Status: " + status);
        });
    };
    $scope.findAll();

    $scope.create = function () {
        console.log($scope.auction);
        AuctionFacade.create($scope.auction)
                .success(function (data) {
                    console.log("Auction was saved.");
                    $location.path("/task");
                }).error(function (data, status) {
            console.log("Error saving auction. Error: " + data + " Status: " + status);
        });
    };

    $scope.delete = function (auction) {
        AuctionFacade.delete(auction).success(function () {
            var index = $scope.auctions.indexOf(auction);

            if (index > -1) {
                $scope.auctions.splice(index, 1);
            }
        }).error(function (data, status) {

            console.log("Ошибка при удалении площадки: " + auction.name + ". Status: " + status);

        });
    };
});

auctionModule.factory('Auction', function () {
    var auction = {
        id: null,
        name: null,
        url: null
    };
    return auction;
});

auctionModule.factory('AuctionFacade', function ($http) {
    var auctionFacade = {};

    auctionFacade.findAll = function () {
        return $http({
            method: "GET",
            withCredentials: true,
            url: "./webresources/auction"
        });
    };

    auctionFacade.create = function (auction) {
        return $http({
            method: "POST",
            data: auction,
            withCredentials: true,
            url: "./webresources/auction"
        });
    };

    auctionFacade.delete = function (auction) {
        return $http({
            method: "DELETE",
            withCredentials: true,
            url: "./webresources/auction/"+auction.id
        });
    };

    return auctionFacade;
});