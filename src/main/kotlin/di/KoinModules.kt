package di

import config.Config
import data.merger.DistributorRepositoryBase
import domain.Distributor
import org.koin.dsl.module

object KoinModules {
    fun core(cfg: Config) = module {
        single<Config> { cfg }
    }
    val baseRepositories = module {

        single<Distributor.Repository> {
            val cfg = get<Config>()
            DistributorRepositoryBase(cfg.mergerHost, cfg.mergerPort)
        }
    }
    val testRepositories = module {

    }
}