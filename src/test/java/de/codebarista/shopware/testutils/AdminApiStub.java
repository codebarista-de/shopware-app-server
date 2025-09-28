package de.codebarista.shopware.testutils;

import de.codebarista.shopware.appserver.AdminApi;
import de.codebarista.shopware.appserver.ShopwareApp;
import de.codebarista.shopware.appserver.service.dto.search.SearchQuery;
import de.codebarista.shopware.appserver.service.dto.search.SearchResult;
import de.codebarista.shopware.appserver.service.dto.sync.SyncResult;
import jakarta.annotation.Nonnull;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.List;

public class AdminApiStub implements AdminApi {
    private Object scriptResult;

    @Override
    public void pushSuccessMessage(ShopwareApp app, String shopId, String message) {

    }

    @Override
    public void pushInfoMessage(ShopwareApp app, String shopId, String message) {

    }

    @Override
    public void pushWarningMessage(ShopwareApp app, String shopId, String message) {

    }

    @Override
    public void pushErrorMessage(ShopwareApp app, String shopId, String message) {

    }

    @Override
    public <T> @Nonnull List<T> search(ShopwareApp app, String shopId, String entityName, SearchQuery query,
                                       Class<? extends SearchResult<T>> responseType, String shopwareLanguageId) {
        return List.of();
    }

    @Override
    public <T> @Nonnull T script(ShopwareApp app, String shopId, String hookName, Object requestBody, Class<T> responseClass) {
        return responseClass.cast(scriptResult);
    }

    @Override
    public @Nonnull SyncResult sync(ShopwareApp app, String shopId, Object requestBody) {
        throw new RuntimeException("Method 'sync' not implemented in stub");
    }

    @Override
    public <T> @Nonnull T sync(ShopwareApp app, String shopId, Object requestBody, Class<T> responseClass) {
        throw new RuntimeException("Method 'sync' not implemented in stub");
    }

    @Override
    public UriComponentsBuilder getShopUrlBuilder(ShopwareApp app, String shopId) {
        throw new RuntimeException("Method 'getShopUrlBuilder' not implemented in stub");
    }

    public void setScriptResult(Object result) {
        scriptResult = result;
    }
}
