package course.concurrency.exams.auction;

import java.util.concurrent.atomic.AtomicReference;

public class AuctionOptimistic implements Auction {

    private final Notifier notifier;

    public AuctionOptimistic(Notifier notifier) {
        this.notifier = notifier;
    }

    private final AtomicReference<Bid> latestBid = new AtomicReference<>(new Bid(-1L, -1L, -1L));

    public boolean propose(Bid bid) {
        Bid currentBid = latestBid.get();
        while (bid.getPrice() > currentBid.getPrice()) {
            do {
                if (latestBid.compareAndSet(currentBid, bid)) {
                    notifier.sendOutdatedMessage(currentBid);
                    return true;
                }
                currentBid = latestBid.get();
            } while (bid.getPrice() > currentBid.getPrice());
        }
        return false;
    }

    public Bid getLatestBid() {
        return latestBid.get();
    }
}
