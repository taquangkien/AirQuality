package com.iot.airsense.service;

import io.github.jav.exposerversdk.*;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    public void sendExpoNotification(String token, String title, String body) throws InterruptedException, PushClientException {

        //  ExponentPushToken[]

        if (!PushClient.isExponentPushToken(token))
            throw new Error("Token:" + token + " is not a valid token.");

        ExpoPushMessage expoPushMessage = new ExpoPushMessage();
        expoPushMessage.getTo().add(token);
        expoPushMessage.setTitle(title);
        expoPushMessage.setBody(body);

        List<ExpoPushMessage> expoPushMessages = new ArrayList<>();
        expoPushMessages.add(expoPushMessage);

        PushClient client = new PushClient();
        List<List<ExpoPushMessage>> chunks = client.chunkPushNotifications(expoPushMessages);

        List<CompletableFuture<List<ExpoPushTicket>>> messageRepliesFutures = new ArrayList<>();

        for (List<ExpoPushMessage> chunk : chunks) {
            messageRepliesFutures.add(client.sendPushNotificationsAsync(chunk));
        }

        // Wait for each completable future to finish
        List<ExpoPushTicket> allTickets = new ArrayList<>();
        for (CompletableFuture<List<ExpoPushTicket>> messageReplyFuture : messageRepliesFutures) {
            try {
                allTickets.addAll(messageReplyFuture.get());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }

        List<ExpoPushMessageTicketPair<ExpoPushMessage>> zippedMessagesTickets = client.zipMessagesTickets(expoPushMessages, allTickets);

        List<ExpoPushMessageTicketPair<ExpoPushMessage>> okTicketMessages = client.filterAllSuccessfulMessages(zippedMessagesTickets);
        String okTicketMessagesString = okTicketMessages.stream().map(
                p -> "Title: " + p.message.getTitle() + ", Id:" + p.ticket.getId()
        ).collect(Collectors.joining(","));
        System.out.println(
                "Recieved OK ticket for " +
                        okTicketMessages.size() +
                        " messages: " + okTicketMessagesString
        );

        List<ExpoPushMessageTicketPair<ExpoPushMessage>> errorTicketMessages = client.filterAllMessagesWithError(zippedMessagesTickets);
        String errorTicketMessagesString = errorTicketMessages.stream().map(
                p -> "Title: " + p.message.getTitle() + ", Error: " + p.ticket.getDetails().getError()
        ).collect(Collectors.joining(","));
        System.out.println(
                "Recieved ERROR ticket for " +
                        errorTicketMessages.size() +
                        " messages: " +
                        errorTicketMessagesString
        );


        // Countdown 30s
        int wait = 5;
        for (int i = wait; i >= 0; i--) {
            System.out.print("Waiting for " + wait + " seconds. " + i + "s\r");
            Thread.sleep(1000);
        }
        System.out.println("Fetching reciepts...");

        List<String> ticketIds = (client.getTicketIdsFromPairs(okTicketMessages));
        CompletableFuture<List<ExpoPushReceipt>> receiptFutures = client.getPushNotificationReceiptsAsync(ticketIds);

        List<ExpoPushReceipt> receipts = new ArrayList<>();
        try {
            receipts = receiptFutures.get();
        } catch (ExecutionException | InterruptedException e) {
            e.printStackTrace();
        }

        System.out.println(
                "Recieved " + receipts.size() + " receipts:");

        for (ExpoPushReceipt reciept : receipts) {
            System.out.println(
                    "Receipt for id: " +
                            reciept.getId() +
                            " had status: " +
                            reciept.getStatus());

        }


    }

}

