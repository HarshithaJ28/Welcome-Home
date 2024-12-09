SELECT AVG(DATEDIFF(Delivered.date, Ordered.orderDate)) as avgProcessingDays FROM Ordered JOIN Delivered ON Ordered.orderID = Delivered.orderID