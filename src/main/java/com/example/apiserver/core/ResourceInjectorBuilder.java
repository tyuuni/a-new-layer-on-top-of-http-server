package com.example.apiserver.core;

public interface ResourceInjectorBuilder<T extends InjectedResource> {
    /**
     * 只需要声明路径就行，因为是从header还是从路径变量里拿到线索是由interceptor决定，使用者只需要定义自己定义的线索，
     * 当然对于有较高统一性的interceptor，也可以不允许有api自定义的线索，比如Authenticator。
     * 另外对于路径有多个路径变量的，实际上应特化为单个能插入多种资源的interceptor，而不是通过require引入多个interceptor，
     * 因为interceptor理论上应该是相互不知道其存在，这样才能避免引入interceptor之间的耦合。
     * 如果interceptor之间有强依赖关系，那么理论上有两种做法：
     *   1. interceptor之间耦合，比如401跟403，403的鉴定依赖401鉴定，403interceptor会直接依赖401interceptor的extractor，
     *      但是这样就有个隐式的要求，所有需要403鉴定的api都必须先引入401，这跟本项目的设计理念是冲突的。
     *   2. interceptor之间不耦合，403interceptor内嵌401鉴定，这样需要403鉴定的api只需要引入403interceptor即可，这样更符合最小化要求。
     *      而403interceptor跟401interceptor之间的最小化只依赖代码组织方式。
     * @param clues 使用方法见上述说明。
     * @return ResourceInjector
     */
    ResourceInjector<T> buildInjector(final String ...clues);
}
