<#import "../parts/bootstrap.ftl" as e>
<#include "../parts/security.ftl">

<@e.main true messageSend??>
    <#include "../parts/user/userBlock.ftl">
    <div class="col-sm-12 col-md-12 col-lg-8">
        <#include "../parts/user/userInfo.ftl">
    </div>
</@e.main>