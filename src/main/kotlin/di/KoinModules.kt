package di

import config.Config
import data.DistributorRepositoryBase
import domain.Distributor
import org.koin.dsl.module

object KoinModules {
    fun main(cfg: Config) = module {
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