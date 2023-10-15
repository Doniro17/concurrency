package course.concurrency.m2_async.cf.min_price;

import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class PriceAggregator {

    private PriceRetriever priceRetriever = new PriceRetriever();
    private ExecutorService executor = Executors.newFixedThreadPool(120);

    public void setPriceRetriever(PriceRetriever priceRetriever) {
        this.priceRetriever = priceRetriever;
    }

    private Collection<Long> shopIds = Set.of(10l, 45l, 66l, 345l, 234l, 333l, 67l, 123l, 768l);

    public void setShops(Collection<Long> shopIds) {
        this.shopIds = shopIds;
    }

    public double getMinPrice(long itemId) {
        List<CompletableFuture<Double>> futures = shopIds.stream().map(shopId -> CompletableFuture.supplyAsync(() -> {
                    return priceRetriever.getPrice(itemId, shopId);
                }, executor)
                .completeOnTimeout(-1.0, 2950, TimeUnit.MILLISECONDS)
                .exceptionally(throwable -> -1.0)).collect(Collectors.toList());

        CompletableFuture<Double> finalMinPrice = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
                .thenApply(ignored -> futures.stream()
                        .map(CompletableFuture::join)
                        .filter(price -> price != -1.0)
                        .mapToDouble(Double::doubleValue)
                        .min()
                        .orElse(Double.NaN));

        return finalMinPrice.join();

    }
}
